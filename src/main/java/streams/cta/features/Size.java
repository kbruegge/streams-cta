package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.hexmap.Shower;

import java.util.HashMap;

/**
 * Calculate the totals amount of photons in the shower. Some call this parameter 'size"
 *
 * Created by jebuss, kbruegge on 24.08.15.
 */
public class Size extends CTACleanedDataProcessor {

    @Override
    public Data process(Data input, HashMap<Integer, Shower> showers) {

        showers.forEach((id, shower) -> {
            double size = shower.pixels.stream().mapToDouble(e -> e.weight).sum();
            input.put("telescope:" + id + ":shower:total_photons", size);
        });

        return input;
    }
}
