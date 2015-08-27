package streams.cta.extraction;

import java.time.LocalDateTime;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

/**
 * Created by jbuss on 25.08.15.
 */
public class PhotonsSignalAmplitude extends CTARawDataProcessor implements StatefulProcessor {

    long nonLSTCounter = 0;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        if (eventData.length != 1855) {
            nonLSTCounter++;
            return input;
        }

        double[] maxVals = (double[]) input.get("maxVal");
        double[] photons = new double[telescope.type.numberOfPixel];

        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
//            This is a totaly artificial calibration of the amplitude
//            the numbers are magic number computed by regression of event amplitudes in streams and hessio
//            photons[pixel] = 5.4236 * maxVals[pixel] + 201.38;
            photons[pixel] = maxVals[pixel];
        }

        input.put("photons", photons);
        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

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
