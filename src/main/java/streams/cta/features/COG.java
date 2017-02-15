package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

import java.util.HashMap;

/**
 * Calculate the center of gravity COG, which is the weighted center of the shower pixels.
 * Created by kbruegge on 14.2.2017
 */
public class COG extends CTACleanedDataProcessor {

    @Override
    public Data process(Data input, HashMap<Integer, Shower> showers) {
        showers.forEach((id, shower) -> {

            CoGPoint coGPoint = calculateCenterOfGravity(shower);

            input.put(String.format("telescope:%d:shower:cog:x", id), coGPoint.cogX);
            input.put(String.format("telescope:%d:shower:cog:y", id), coGPoint.cogY);
        });

        return input;
    }

    public CoGPoint calculateCenterOfGravity(Shower shower){
        double cogX = 0;
        double cogY = 0;
        double size = 0;

        // find weighted center of the shower pixels.
        for (Shower.Pixel pixel : shower.pixels) {
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