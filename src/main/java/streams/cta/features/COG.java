package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

import static streams.hexmap.Shower.*;

/**
 * Calculate the center of gravity COG, which is the weighted center of the shower pixels.
 *
 * Created by kbruegge on 14.2.2017
 */
public class COG extends CTACleanedDataProcessor {

    @Override
    public Data process(Data input, Shower shower) {
        CoGPoint coGPoint = calculateCenterOfGravity(shower);
        input.put("shower:cog:x", coGPoint.cogX);
        input.put("shower:cog:y", coGPoint.cogY);
        return input;
    }

    public CoGPoint calculateCenterOfGravity(Shower shower) {
        double cogX = 0;
        double cogY = 0;
        double size = 0;

        // find weighted center of the shower pixels.
        for (SignalPixel pixel : shower.signalPixels) {
            cogX += pixel.xPositionInMM * pixel.weight;
            cogY += pixel.yPositionInMM * pixel.weight;
            size += pixel.weight;
        }

        cogX /= size;
        cogY /= size;

        return new CoGPoint(cogX, cogY);
    }


    class CoGPoint {
        final double cogX;
        final double cogY;

        private CoGPoint(double cogX, double cogY) {
            this.cogX = cogX;
            this.cogY = cogY;
        }

        @Override
        public String toString() {
            return "CoGPoint{" +
                    "cogX=" + cogX +
                    ", cogY=" + cogY +
                    '}';
        }
    }
}