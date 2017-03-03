package streams;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.util.List;
import java.util.Set;

/**
 * Split the data items by telescopes. Recieves on data item with many telescopes like
 * 'telescope:*:*' and creates a new data item for each telescope. The telescope prefix will be
 * dropped first. For example
 *
 * 'telescope:2:shower:width'
 *
 * will become
 *
 * 'shower:width'
 *
 * Created by kbruegge on 2/17/17.
 */
public class SplitByTelescope implements Processor {

    @Parameter(description = "Save collected telescopes under this key.", required = true)
    String key = "@telescopes";

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Data process(Data item) {

        int[] triggeredTelescopes = (int[]) item.get("array:triggered_telescopes");

        //collect all telescopes first in the list
        Data[] items = new Data[triggeredTelescopes.length];
        int i = 0;

        //create a new data item for each triggered telescope
        for (int id : triggeredTelescopes) {
            Data data = DataFactory.create();

            Set<String> selectedKeys = Keys.select(item, "telescope:" + id + ":*");
            for (String key : selectedKeys) {

                List<String> splitToList = Splitter.on(":")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(key);

                //remove first two elements (telescope:*:)
                List<String> subList = splitToList.subList(2, splitToList.size());

                String newKey = Joiner.on(":").join(subList);
                data.put(newKey, item.get(key));
                item.remove(key);
            }

            for (String key: Keys.select(item, "array:*")){
                data.put(key, item.get(key));
            }
            data.put("telescope:id", id);
            items[i++] = data;
        }

        item.put(key, items);

        return item;
    }
}
