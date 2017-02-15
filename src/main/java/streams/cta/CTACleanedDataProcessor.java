package streams.cta;

import stream.Data;
import stream.Keys;
import stream.Processor;
import streams.hexmap.Shower;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by kbruegge on 14.2.17.
 */
public abstract class CTACleanedDataProcessor implements Processor{

    @Override
    public Data process(Data input) {

        HashMap<Integer, Shower> map = new HashMap<>();

        int[] triggeredTelescopes = (int[]) input.get("triggered_telescopes:ids");

        for(int id : triggeredTelescopes){
            String key = "telescope:" + id +  ":shower";
            if (input.get(key) != null) {
                Shower shower = (Shower) input.get(key);
                map.put(id, shower);
            }
        }
        return process(input, map);
    }

    public abstract Data process(Data input, HashMap<Integer, Shower> showers);
}
