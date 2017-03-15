package streams.cta.features;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

/**
 * Calculate the Width, Length and Delta from the spacial distribution of shower pixels, by use of
 * the covariance Matrix and its Eigenvalues of it.
 *
 * Created by jbuss and kbruegge on 27.08.15.
 */
public class WidthLengthDelta extends CTACleanedDataProcessor {


    @Override
    public Data process(Data input, Shower shower) {

        double size = (double) input.get("shower:total_photons");
        double cogX = (double) input.get("shower:cog:x");
        double cogY = (double) input.get("shower:cog:y");


        // Calculate the weighted Empirical variance along the x and y axis.
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(shower, cogX, cogY);

        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
        // turns out the eigenvalues describe the variance in the eigenbasis of
        // the covariance matrix

        double varianceLong = eig.getRealEigenvalue(0) / size;
        double varianceTrans = eig.getRealEigenvalue(1) / size;

        double length = Math.sqrt(varianceLong);
        double width = Math.sqrt(varianceTrans);
        double delta = calculateDelta(eig);

        input.put("shower:length", length);
        input.put("shower:width", width);
        input.put("shower:delta", delta);

        return input;
    }

    /**
     * calculate the covariance Matrix of the shower distribution.
     *
     * @param shower the shower to calculate the covariance for
     * @param cogX   the x coordinate of the showers center of gravity
     * @param cogY   the y coordinate of the center of grtavity
     * @return the covariance matrix weighted by the individual pixel weights
     */
    private RealMatrix calculateCovarianceMatrix(Shower shower, double cogX, double cogY) {

        double variance_xx = 0;
        double variance_yy = 0;
        double covariance_xy = 0;

        for (Shower.SignalPixel pix : shower.signalPixels) {

            double posx = pix.xPositionInMM;
            double posy = pix.yPositionInMM;

            variance_xx += pix.weight * (posx - cogX) * (posx - cogX);
            variance_yy += pix.weight * (posy - cogY) * (posy - cogY);
            covariance_xy += pix.weight * (posx - cogX) * (posy - cogY);

        }

        double[][] matrixData = {{variance_xx, covariance_xy},
                {covariance_xy, variance_yy}};
        return MatrixUtils.createRealMatrix(matrixData);
    }

    /**
     * calculate the angle between the eigenvector and the camera axis. So basically the angle
     * between the major-axis of the ellipse and the camrera axis. This will be written in radians.
     *
     * @param eig 'Eigenvalue decomposition of the shower distribution'
     * @return (double) delta 'rotation angle of the shower'
     */
    private double calculateDelta(EigenDecomposition eig) {

        double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
        double transversalComponent = eig.getEigenvector(0).getEntry(1);
        return Math.atan(transversalComponent / longitudinalComponent);
    }

}
