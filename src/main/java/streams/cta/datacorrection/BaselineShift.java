package streams.cta.datacorrection;

import java.time.LocalDateTime;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

/**
 * Created by jbuss on 25.08.15.
 */
public class BaselineShift extends CTARawDataProcessor implements StatefulProcessor {

    int bslCorrectionFactor = 100;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        int[][] correctedEventData = new int[telescope.type.numberOfPixel][eventData[0].length];

        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
            for (int slice = 0; slice < eventData[pixel].length; slice++) {
                correctedEventData[pixel][slice] = eventData[pixel][slice] - bslCorrectionFactor;
            }
        }

        input.put("correctedEventData", correctedEventData);

        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
