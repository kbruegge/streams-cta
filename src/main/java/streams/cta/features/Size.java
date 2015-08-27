package streams.cta.features;

import java.time.LocalDateTime;
import java.util.HashSet;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.CameraPixel;

/**
 * Created by jebuss, kbruegge on 24.08.15.
 */
public class Size extends CTACleanedDataProcessor {

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons,
                        double[] arrivalTimes, HashSet<CameraPixel> showerPixel) {

        double  size  = showerPixel.stream().mapToDouble(pix -> photons[pix.id]).sum();

        input.put("size", size);
        return input;
    }
}
