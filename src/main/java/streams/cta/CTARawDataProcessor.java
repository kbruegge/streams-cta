package streams.cta;

import stream.Data;
import stream.Processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract processor class that extracts event data, telescope and timestamp information from a
 * telescope event and calls process method with those extracted values. New processors can
 * implement new processors based on this one using a process method with already extracted values.
 *
 * @author kai
 */
public abstract class CTARawDataProcessor implements Processor {

    @Override
    public Data process(Data input) {

        String key = "raw:photons";
        if (input.containsKey(key)) {
            double[] image = (double[]) input.get(key);
            return process(input, image);
        }
        return null;
    }

    public abstract Data process(Data input, double[] image);
}
