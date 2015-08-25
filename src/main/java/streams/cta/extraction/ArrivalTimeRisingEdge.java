package streams.cta.extraction;

import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

import java.time.LocalDateTime;

/**
 * Created by jbuss on 25.08.15.
 */
public class ArrivalTimeRisingEdge extends CTARawDataProcessor implements StatefulProcessor {

    long nonLSTCounter = 0;
    int searchWindowSize = 20;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        if(eventData.length != 1855){
            nonLSTCounter++;
            return input;
        }

        IntervalMarker[] m = new IntervalMarker[telescope.type.numberOfPixel];

        int[] maxPos = (int[]) input.get("maxPos");

        double[] arrivalTimes = new double[telescope.type.numberOfPixel];
        for (int pixel = 0; pixel < telescope.type.numberOfPixel; pixel++) {
            double arrivalTime  = 0;
            double maxSlope     = 0;

            for (int slice = maxPos[pixel] - searchWindowSize;
                 slice+2 <= eventData[pixel].length && slice < maxPos[pixel]; slice++) {

                if(slice-2 < 0){
                    continue;
                }

                double current_slope = eventData[pixel][slice + 2] - eventData[pixel][slice - 2];

                if(current_slope > maxSlope){
                    arrivalTime = slice;
                }
            }
            arrivalTimes[pixel] = arrivalTime;
            m[pixel] = new IntervalMarker(arrivalTime,arrivalTime + 1);
        }
        input.put("arrivalTimes", arrivalTimes);
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
