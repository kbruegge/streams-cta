package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

import java.util.HashMap;

import static java.lang.Math.*;

/**
 * This will be a basic translation of the code found in ctapipe.
 * Some reference is in here: http://adsabs.harvard.edu/abs/1993ApJ...404..206R
 * (Whipple&Reynolds et al. 1993)
 *
 * Created by kbruegge on 2/15/17.
 */
public class Moments extends CTACleanedDataProcessor{


    @Override
    public Data process(Data input, HashMap<Integer, Shower> showers) {

        showers.forEach((telescopeId, shower) -> {
           double size = shower.pixels.stream().mapToDouble(e -> e.weight).sum();
            double sumX = 0;
            double sumY = 0;

            // find weighted center of the shower pixels.
            for (Shower.Pixel pixel : shower.pixels) {
                sumX += pixel.xPositionInMM * pixel.weight;
                sumY += pixel.yPositionInMM * pixel.weight;
                size += pixel.weight;
            }

            final double meanX = sumX /= size;
            final double meanY = sumY /= size;

//            final double cogX = meanX;
////
            //calculate some variances
            double sxxx = 0, syyy = 0, sxyy = 0, sxxy = 0, sxx = 0, syy = 0, sxy = 0;
            for (Shower.Pixel p : shower.pixels){
                sxx += p.weight * pow((p.xPositionInMM - meanX), 2);
                syy += p.weight * pow((p.yPositionInMM - meanY), 2);
                sxy += p.weight * (p.xPositionInMM - meanX) * (p.yPositionInMM - meanY);

                sxxx += p.weight * pow((p.xPositionInMM - meanX), 3);
                syyy += p.weight * pow((p.yPositionInMM - meanY), 3);

                sxyy += p.weight * (p.xPositionInMM - meanX) * pow((p.yPositionInMM - meanY), 2);
                sxxy += p.weight * (p.yPositionInMM - meanY) * pow((p.xPositionInMM - meanX), 2);
            }

            sxx /= size;
            syy /= size;
            sxy /= size;

            sxxx /= size;
            syyy /= size;
            sxyy /= size;
            sxxy /= size;

            //now analytically calculate the eigenvalues and vectors.
            double d0 = syy - sxx;
            double d1 = 2 * sxy;
            double d2 = d0 + sqrt(d0*d0 + d1*d1);
            double a = d2 / d1;

            //apperently things can get less than zero. just set to  zero then.
            double width = sqrt(min((syy + a*a*sxx - 2*a*sxy) / (1 + a*a), 0));
            double length= sqrt(min((sxx + a*a*syy - 2*a*sxy) / (1 + a*a), 0));

            double delta = atan(a);
            double cos_delta = 1 / sqrt(1 + a*a);
            double sin_delta = a * cos_delta;

            //I dont know what this is
            double b = meanY - a * meanX;
            double miss = abs(b/(sqrt(1 + a*a)));
            double r = sqrt(meanX*meanX + meanY*meanY);
            double phi = atan2(meanY, meanX); //wtf?

            //calculate higher order moments
            double skewness_a = 0, skewness_b = 0, kurtosis_a = 0, kurtosis_b = 0;
            for (Shower.Pixel p : shower.pixels){
                double sk = cos_delta * (p.xPositionInMM - meanX) + sin_delta * (p.yPositionInMM - meanY);
                skewness_a += p.weight *pow(sk, 3);
                skewness_b += p.weight *pow(sk, 2);

                kurtosis_a += p.weight *pow(sk, 4);
                kurtosis_b += p.weight *pow(sk, 2);
            }

            double skewness = (skewness_a/size) / pow(skewness_b/size, 3.0/2.0);
            double kurtosis = (kurtosis_a/size) / pow(kurtosis_b/size, 2);

        });

//        public
//
//    # Compute major axis line representation y = a * x + b and correlations
//        S_xx = np.sum(image * (pix_x - mean_x) ** 2) / size
//        S_yy = np.sum(image * (pix_y - mean_y) ** 2) / size
//        S_xy = np.sum(image * (pix_x - mean_x) * (pix_y - mean_y)) / size
//        S_xxx = np.sum(image * (pix_x - mean_x) ** 3) / size
//        S_yyy = np.sum(image * (pix_y - mean_y) ** 3) / size
//        S_xyy = np.sum(image * (pix_x - mean_x) * (pix_y - mean_y) ** 2) / size
//        S_xxy = np.sum(image * (pix_y - mean_y) * (pix_x - mean_x) ** 2) / size
//        S_x4 = np.sum(image * (pix_x - mean_x) ** 4) / size
//        S_y4 = np.sum(image * (pix_y - mean_y) ** 4) / size
//
//    # Sanity check2:
//
//    # If S_xy=0 (which should happen not very often, because Size>0)
//    # we cannot calculate Length and Width.  In reallity it is almost
//    # impossible to have a distribution of cerenkov photons in the
//    # used pixels which is exactly symmetric along one of the axis
//        if S_xy == 0:
//        raise HillasParameterizationError(("X and Y uncorrelated. Cannot "
//        "calculate length & width"))
//
//        d0 = S_yy - S_xx
//        d1 = 2 * S_xy
//    # temp = d * d + 4 * S_xy * S_xy
//        d2 = d0 + np.sqrt(d0 * d0 + d1 * d1)
//        a = d2 / d1
//    # Angle between ellipse major ax. and x-axis of camera.
//    # Will be used for disp
//        delta = np.arctan(a)
//        b = mean_y - a * mean_x
//    # Sin & Cos Will be used for calculating higher order image parameters
//        cos_delta = 1 / np.sqrt(1 + a * a)
//        sin_delta = a * cos_delta
//
//    # Compute Hillas parameters
//         width_2 = (S_yy + a * a * S_xx - 2 * a * S_xy) / (1 + a * a)  # width squared
//        length_2 = (S_xx + a * a * S_yy + 2 * a * S_xy) / (1 + a * a)  # length squared
//
//    # Sanity check3:
//        width = 0. if width_2 < 0. else np.sqrt(width_2)
//        length = 0. if length_2 < 0. else np.sqrt(length_2)
//
//        miss = np.abs(b / np.sqrt(1 + a * a))
//        r = np.sqrt(mean_x * mean_x + mean_y * mean_y)
//        phi = np.arctan2(mean_y, mean_x)
//
//    # Higher order moments
//                sk = cos_delta * (pix_x - mean_x) + sin_delta * (pix_y - mean_y)
//
//        skewness = ((np.sum(image * np.power(sk, 3)) / size) /
//                ((np.sum(image * np.power(sk, 2)) / size) ** (3. / 2)))
//        kurtosis = ((np.sum(image * np.power(sk, 4)) / size) /
//                ((np.sum(image * np.power(sk, 2)) / size) ** 2))
//        asym3 = (np.power(cos_delta, 3) * S_xxx
//                + 3.0 * np.power(cos_delta, 2) * sin_delta * S_xxy
//                + 3.0 * cos_delta * np.power(sin_delta, 2) * S_xyy
//                + np.power(sin_delta, 3) * S_yyy)
//        asym = - np.power(-asym3, 1. / 3) if (asym3 < 0.) \
//        else np.power(asym3, 1. / 3)
//
//        assert np.sign(skewness) == np.sign(asym)
//
//    # another definition of assymetry
//    # asym = (mean_x - pix_x[np.argmax(image)]) * cos_delta
//    #      + (mean_y - pix_y[np.argmax(image)]) * sin_delta
//
//    # # Compute azwidth by transforming to (p, q) coordinates
//    # sin_theta = mean_y / r
//    # cos_theta = mean_x / r
//    # q = (mean_x - pix_x) * sin_theta + (pix_y - mean_y) * cos_theta
//    # m_q = np.sum(image * q) / size
//    # m_qq = np.sum(image * q * q) / size
//    # azwidth_2 = m_qq - m_q * m_q
//    # azwidth = np.sqrt(azwidth_2)
//
//        return MomentParameters(size=size,
//                cen_x=mean_x*unit,
//                cen_y=mean_y*unit,
//                length=length*unit,
//                width=width*unit,
//                r=r*unit,
//                phi=Angle(phi*u.rad),
//                psi=Angle(delta*u.rad),
//                miss=miss*unit,
//                skewness=skewness,
//                kurtosis=kurtosis)
    }
}
