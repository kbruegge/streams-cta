package streams.cta.extraction;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

import java.time.LocalDateTime;

/**
 * Created by kai on 11.08.15.
 */
public class Photons extends CTARawDataProcessor implements StatefulProcessor {

    double[] photons;
    long nonLSTCounter = 0;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        if(eventData.length != 1855){
            nonLSTCounter++;
            return input;
        }

        photons = new double[telescope.type.numberOfPixel];
        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
            double sum = 0;
            for (int slice = 0; slice < eventData[pixel].length; slice++) {
                sum += eventData[pixel][slice];
            }
            photons[pixel] = sum;
        }
        input.put("photons", photons);
        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {

    }

    @Override
    public void resetState() throws Exception {
        nonLSTCounter = 0;
    }

    @Override
    public void finish() throws Exception {
        System.out.println("Non LST telescope events: " + nonLSTCounter);
    }
}
