package streams;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 */
public class MergeByTelescope implements Processor {

    @Parameter(description = "Save collected telescopes under this key.", required = true)
    String key = "@telescopes";

    @Override
    public Data process(Data item) {
        Data[] triggeredTelescopes = (Data[]) item.get(key);

        //create a new data item for each triggered telescope
        for (Data telescope : triggeredTelescopes) {
            for (String key : telescope.keySet()) {
                item.put(key, item.get(key));
            }

        }

        return item;
    }
}
