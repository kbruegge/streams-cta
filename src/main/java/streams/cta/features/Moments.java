package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

import java.util.HashMap;

import static java.lang.Math.*;

/**
 * This will be a basic translation of the code found in ctapipe. Some reference is in here:
 * http://adsabs.harvard.edu/abs/1993ApJ...404..206R (Whipple&Reynolds et al. 1993)
 *
 * Created by kbruegge on 2/15/17.
 */
public class Moments extends CTACleanedDataProcessor {


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

            final double meanX = sumX / size;
            final double meanY = sumY / size;

            //calculate the covariance matrix
            double sxx = 0, syy = 0, sxy = 0;
            for (Shower.Pixel p : shower.pixels) {
                sxx += p.weight * pow((p.xPositionInMM - meanX), 2);
                syy += p.weight * pow((p.yPositionInMM - meanY), 2);
                sxy += p.weight * (p.xPositionInMM - meanX) * (p.yPositionInMM - meanY);
            }

            sxx /= size;
            syy /= size;
            sxy /= size;

            //now analytically calculate the eigenvalues and vectors.
            double d0 = syy - sxx;
            double d1 = 2 * sxy;
            double d2 = d0 + sqrt(d0 * d0 + d1 * d1);
            double a = d2 / d1;

            //apperently things can get less than zero. just set to  zero then.
            double width = sqrt(min((syy + a * a * sxx - 2 * a * sxy) / (1 + a * a), 0));
            double length = sqrt(min((sxx + a * a * syy - 2 * a * sxy) / (1 + a * a), 0));

            double delta = atan(a);
            double cos_delta = 1 / sqrt(1 + a * a);
            double sin_delta = a * cos_delta;

            //I dont know what this is
            double b = meanY - a * meanX;
            double miss = abs(b / (sqrt(1 + a * a)));
            double r = sqrt(meanX * meanX + meanY * meanY);
            double phi = atan2(meanY, meanX); //wtf?

            //calculate higher order moments
            double skewness_a = 0, skewness_b = 0, kurtosis_a = 0, kurtosis_b = 0;
            for (Shower.Pixel p : shower.pixels) {
                double sk = cos_delta * (p.xPositionInMM - meanX) + sin_delta * (p.yPositionInMM - meanY);
                skewness_a += p.weight * pow(sk, 3);
                skewness_b += p.weight * pow(sk, 2);

                kurtosis_a += p.weight * pow(sk, 4);
                kurtosis_b += p.weight * pow(sk, 2);
            }

            double skewness = (skewness_a / size) / pow(skewness_b / size, 3.0 / 2.0);
            double kurtosis = (kurtosis_a / size) / pow(kurtosis_b / size, 2);

            input.put("telescope:" + telescopeId + ":shower:width", width);
            input.put("telescope:" + telescopeId + ":shower:length", length);
            input.put("telescope:" + telescopeId + ":shower:delta", delta);
            input.put("telescope:" + telescopeId + ":shower:psi", delta); // i've found both names for the smae thing
            input.put("telescope:" + telescopeId + ":shower:skewness", skewness);
            input.put("telescope:" + telescopeId + ":shower:kurtosis", kurtosis);
            input.put("telescope:" + telescopeId + ":shower:phi", phi);
            input.put("telescope:" + telescopeId + ":shower:miss", miss);
            input.put("telescope:" + telescopeId + ":shower:r", r);
            input.put("telescope:" + telescopeId + ":shower:cog:x", meanX);
            input.put("telescope:" + telescopeId + ":shower:cog:Y", meanY);

        });

        return input;
    }
}
