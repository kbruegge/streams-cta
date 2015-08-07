package streams.cta;

import java.time.LocalDateTime;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by kai on 02.06.15.
 */
public abstract class CTARawDataProcessor implements Processor {

    @Parameter(description = "The key under which to find the CTA raw data in the data item.", required = false,
            defaultValue = "data")
    private String eventKey = "data";

    @Override
    public Data process(Data input) {
        LocalDateTime timeStamp = (LocalDateTime) input.get("@timestamp");
        CTATelescope telescope = (CTATelescope) input.get("@telescope");

        short[][] data = (short[][]) input.get(eventKey);
        if (data != null && data[0] != null && timeStamp != null && telescope != null) {
            return process(input, telescope, timeStamp, data);
        }
        return null;
    }

    public abstract Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData);
}
