package streams.cta.features;

import stream.Data;
import streams.cta.CTACleanedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.CTAHexPixelMapping;
import streams.hexmap.CameraPixel;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Calculate the center of gravity COG, which is the weighted center of the shower pixels.
 * Created by jbuss on 27.08.15.
 */
public class COG extends CTACleanedDataProcessor{
    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons, double[] arrivalTimes, HashSet<CameraPixel> showerPixel) {


        double size = (double) input.get("size");

        double[] cog = { 0, 0 };
        // find weighted center of the shower pixels.
        for (CameraPixel pixel : showerPixel) {
            cog[0] += photons[pixel.id] * pixel.xPositionInMM;
            cog[1] += photons[pixel.id] * pixel.yPositionInMM;
        }
        cog[0] /= size;
        cog[1] /= size;

        input.put("cog", cog);

        return input;
    }
}
