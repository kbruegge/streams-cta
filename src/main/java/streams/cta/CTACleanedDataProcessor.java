package streams.cta;

import stream.Data;
import stream.Processor;
import streams.hexmap.Shower;

/**
 * Abstract processor class that extracts shower event data from a telescope event and
 * calls process method with those extracted values. New processors can implement new processors
 * based on this one using a process method with already extracted values.
 *
 * Created by kbruegge on 14.2.17.
 */
public abstract class CTACleanedDataProcessor implements Processor{

    @Override
    public Data process(Data input) {

        String key = "shower";
        if (input.get(key) != null) {
            Shower shower = (Shower) input.get(key);
            return process(input, shower);
        }
        return null;
    }

    public abstract Data process(Data input, Shower shower);
}
