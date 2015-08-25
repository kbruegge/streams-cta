package streams.cta.extraction;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

import java.time.LocalDateTime;

/**
 * Created by jebuss on 20.08.15.
 * Find the maximum amplitude in a pixel and extract its amplitude value and the postition of the maximum as timeslice
 */
public class MaxAmplitude extends CTARawDataProcessor implements StatefulProcessor {

    long nonLSTCounter = 0;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        double[] maxPos;
        double[] maxVal;
        double[] corrAmpl;

        if(eventData.length != 1855){
            nonLSTCounter++;
            return input;
        }

        maxPos      = new double[telescope.type.numberOfPixel];
        maxVal      = new double[telescope.type.numberOfPixel];
        corrAmpl    = new double[telescope.type.numberOfPixel];

        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
            short max  = 0;
            double arrivalTime  = 0;
            for (int slice = 0; slice < eventData[pixel].length; slice++) {
                short value = eventData[pixel][slice];
                if(value > max){
                    arrivalTime = slice;
                    max = value;
                }
            }
            maxVal[pixel]   = max;
            maxPos[pixel]   = arrivalTime;
        }
        input.put("maxPos", maxPos);
        input.put("maxVal", maxVal);
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
