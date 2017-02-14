package streams.cta;

import java.time.LocalDateTime;
import java.util.Map;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Abstract processor class that extracts event data, telescope and timestamp information from a
 * telescope event and calls process method with those extracted values. New processors can
 * implement new processors based on this one using a process method with already extracted values.
 *
 * @author kai
 */
public abstract class CTARawDataProcessor implements Processor {
//
//    @Parameter(description = "The key under which to find the CTA raw data in the data item.",
//            required = false)
//    private String eventKey = "@raw_data";

    @Override
    public Data process(Data input) {

        Map<Integer, double[]> data = (Map<Integer, double[]>) input.get("images");

        if (data != null) {
            return process(input, data);
        }
        return null;
    }

    public abstract Data process(Data input, Map<Integer, double[]> images);
}
