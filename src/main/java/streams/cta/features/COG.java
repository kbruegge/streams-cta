package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.Shower;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Calculate the center of gravity COG, which is the weighted center of the shower pixels.
 * Created by kbruegge on 14.2.2017
 */
public class COG extends CTACleanedDataProcessor{

    @Override
    public Data process(Data input, HashMap<Integer, Shower> showers) {
        showers.forEach((id, shower ) -> {

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

            input.put(String.format("shower:%d:cog:x", id), cogX);
            input.put(String.format("shower:%d:cog:y", id), cogY);

        });

        return input;
    }
}
