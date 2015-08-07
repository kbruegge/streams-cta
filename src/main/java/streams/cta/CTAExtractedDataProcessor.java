package streams.cta;

import java.time.LocalDateTime;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import streams.cta.container.ExtractedData;

/**
 * Created by kai on 27.07.15.
 */
public abstract class CTAExtractedDataProcessor implements Processor {


    @Parameter(description = "The key under which to find the extracted data in the data item.", required = false,
            defaultValue = "extractedData")
    private String eventKey = "extractedData";

    @Override
    public Data process(Data input) {
        LocalDateTime timeStamp = (LocalDateTime) input.get("@timestamp");
        CTATelescope telescope = (CTATelescope) input.get("@telescope");

        ExtractedData data = (ExtractedData) input.get(eventKey);
        if (data != null && timeStamp != null && telescope != null) {
            return process(input, telescope, timeStamp, data);
        }
        return null;
    }

    public abstract Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, ExtractedData eventData);
}
