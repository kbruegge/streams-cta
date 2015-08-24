package streams.cta;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import streams.hexmap.CTAHexPixelMapping;
import streams.hexmap.CameraPixel;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Created by jebuss on 24.08.15.
 */
public abstract class CTACleanedDataProcessor extends CTAExtractedDataProcessor {


    @Parameter(description = "The key under which to find the cleaned data in the data item.", required = false,
            defaultValue = "cleanedData")
    private String eventKey = "cleanedData";


    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons, double[] arrivalTimes) {
        HashSet<CameraPixel> showerPixel = (HashSet<CameraPixel> ) input.get("shower");

        if (showerPixel != null) {
            return process(input, telescope, timeStamp, photons, arrivalTimes, showerPixel);
        }
        return null;
    }

    public abstract Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons, double[] arrivalTimes, HashSet<CameraPixel> showerPixel);
}
