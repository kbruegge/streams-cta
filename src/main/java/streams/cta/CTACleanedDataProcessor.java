package streams.cta;

import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import streams.hexmap.Shower;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kbruegge on 14.2.17.
 */
public abstract class CTACleanedDataProcessor implements Processor{

    public static void putShowerFeature(Data input, Serializable data, String name, int telescopeId){
        input.put(String.format("shower:%d:%s", telescopeId, name), data);
    }

    @Override
    public Data process(Data input) {

        Set<String> select = Keys.select(input, "shower:*");
        HashMap<Integer, Shower> map = new HashMap<>();

        select.forEach(key -> {
            Shower shower = (Shower) input.get(key);
            map.put(shower.cameraId, shower);
        });

        return process(input, map);
    }

    public abstract Data process(Data input, HashMap<Integer, Shower> showerPixel);
}
