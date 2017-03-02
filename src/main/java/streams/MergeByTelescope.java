package streams;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Using the 'key' attribute, this processor merges the array of telescopes saved as in the data
 * item into the original data item. It should be applied after SplitByTelescope which should be
 * combined with the ForEach processor list.
 */
public class MergeByTelescope implements Processor {

    @Parameter(description = "Save collected telescopes under this key.", required = true)
    String key = "@telescopes";

    @Override
    public Data process(Data item) {
        Data[] triggeredTelescopes = (Data[]) item.get(key);

        //create a new data item for each triggered telescope
        for (Data telescope : triggeredTelescopes) {
            int id = (int) telescope.get("telescope:id");
            String prefix = "telescope:" + id + ":";
            for (String key : telescope.keySet()) {
                item.put(prefix + key, telescope.get(key));
            }
            item.remove("telescope:" + id + ":telescope:id");
        }

        return item;
    }
}
