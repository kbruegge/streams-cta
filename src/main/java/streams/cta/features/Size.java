package streams.cta.features;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.Shower;

/**
 * Created by jebuss, kbruegge on 24.08.15.
 */
public class Size extends CTACleanedDataProcessor {

    @Override
    public Data process(Data input, HashMap<Integer, Shower> showers) {

        showers.forEach((id, shower) ->{
            double size = shower.pixels.stream().mapToDouble(e -> e.weight).sum();
            input.put(String.format("shower:%d:total_photons", id), size);
        });

        return input;
    }
}
