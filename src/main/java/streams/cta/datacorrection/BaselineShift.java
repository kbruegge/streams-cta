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
public class BaselineShift extends CTARawDataProcessor  {

    short bslCorrectionFactor = 100;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {


        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
            for (int slice = 0; slice < eventData[pixel].length; slice++) {
                eventData[pixel][slice] = (short) (eventData[pixel][slice] - bslCorrectionFactor);
            }
        }

        return input;
    }

}
