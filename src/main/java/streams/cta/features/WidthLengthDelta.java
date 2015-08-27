package streams.cta.features;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.CTAHexPixelMapping;
import streams.hexmap.CameraPixel;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Calculate the Width, Length and Delta from the spacial distribution of shower pixels,
 * by use of the covariance Matrix and ther Eigenvalue decomposition of it.
 *
 * Created by jbuss on 27.08.15.
 */
public class WidthLengthDelta extends CTACleanedDataProcessor{
    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons,
                        double[] arrivalTimes, HashSet<CameraPixel> showerPixel) {

        CTAHexPixelMapping pixelMap = CTAHexPixelMapping.getInstance();

        if( !input.containsKey("@size") && !input.containsKey("@cog")){
            return input;
        }

        double  size = (double) input.get("@size");
        double[] cog = (double []) input.get("@cog");

        // Calculate the weighted Empirical variance along the x and y axis.
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel, photons, cog);
        // get the eigenvalues and eigenvectors of the matrix and weigh them
        // accordingly.
        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
        // turns out the eigenvalues describe the variance in the eigenbasis of
        // the covariance matrix

        double varianceLong = eig.getRealEigenvalue(0) / size;
        double varianceTrans = eig.getRealEigenvalue(1) / size;

        double length = Math.sqrt(varianceLong);
        double width  = Math.sqrt(varianceTrans);
        double delta  = calculateDelta(eig);

        input.put("@length", length);
        input.put("@width", width);
        input.put("@delta", delta);

        return input;
    }

    /**
     *  calculate the covariance Matrix of the shower distribution.
     *
     *  @param showerPixel
     *  'Set of showerpixels'
     *  @param photons
     *  'Array with photon weights of all pixels'
     *  @param cog
     *  'Center of gravity of the light distribtuion in the shower'
     *
     *  @return covariance Matrix (RealMatrix)
     */
    public RealMatrix calculateCovarianceMatrix(HashSet<CameraPixel> showerPixel,
                                                double[] photons, double[] cog) {
        double variance_xx = 0;
        double variance_yy = 0;
        double covariance_xy = 0;

        for (CameraPixel pix : showerPixel) {
            double weight = photons[pix.id];

            double posx = pix.xPositionInMM;
            double posy = pix.yPositionInMM;

            variance_xx += weight * (posx - cog[0]) * (posx - cog[0]);
            variance_yy += weight * (posy - cog[1]) * (posy - cog[1]);
            covariance_xy += weight * (posx - cog[0]) * (posy - cog[1]);

        }

        double[][] matrixData = { { variance_xx, covariance_xy },
                { covariance_xy, variance_yy } };
        return MatrixUtils.createRealMatrix(matrixData);
    }

    /**
     *  calculate the angle between the eigenvector and the camera axis.
     *  So basicly the angle between the major-axis of the ellipse and the camrera axis.
     *  This will be written in radians.
     *
     *  @param eig 'Eigenvalue decomposition of the shower distribution'
     *  @return (double) delta 'rotation angle of the shower'
     */
    public double calculateDelta(EigenDecomposition eig) {

        double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
        double transversalComponent = eig.getEigenvector(0).getEntry(1);
        return Math.atan(transversalComponent / longitudinalComponent);
    }
}
