package streams.cta;

import java.time.LocalDateTime;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Abstract processor class that implements extracting basic information from a telescope event and
 * calls process method with those extracted values. New processors can implement new processors
 * based on this one using a process method with already extracted values.
 *
 * @author kai
 */
public abstract class CTAExtractedDataProcessor implements Processor {


    @Parameter(description = "The key under which to find the extracted data in the data item.",
            required = false,
            defaultValue = "extractedData")
    private String eventKey = "extractedData";

    @Override
    public Data process(Data input) {
        LocalDateTime timeStamp = (LocalDateTime) input.get("@timestamp");
        CTATelescope telescope = (CTATelescope) input.get("@telescope");

        double[] photons = (double[]) input.get("photons");
        double[] arrivalTimes = (double[]) input.get("arrivalTimes");
        if (photons != null && arrivalTimes != null && timeStamp != null && telescope != null) {
            return process(input, telescope, timeStamp, photons, arrivalTimes);
        }
        return null;
    }

    public abstract Data process(Data input, CTATelescope telescope,
                                 LocalDateTime timeStamp, double[] photons, double[] arrivalTimes);
}
