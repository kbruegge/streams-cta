package streams.cta;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import scala.Int;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Using the 'key' attribute, this processor collects the single telescope data saved in the data
 * item into back into the original data item containing the multi telescope events.
 * This is useful when reading in per telescope image feature data from a CSV file or something similar.
 *
 */
public class CollectByEvent implements Processor {

    @Parameter(description = "Save collected telescopes under this key.", required = false)
    String key = "unique_event_id";

    private Integer previousId = null;
    private List<Data> items = new ArrayList<>();

    @Override
    public Data process(Data inputItem) {
        if (previousId == null){
            previousId = (Integer) inputItem.get(key);
        }
        int id = (int) inputItem.get(key);
        if (id == previousId)
        {
            items.add(inputItem);
            return null;
        }
        else {
            Data outputItem = DataFactory.create();
            List<Integer> triggeredTelescopes = Lists.newArrayList();

            for (Data telescopeData : items) {
                int telescopeId = (int) telescopeData.get("telescope:id");
                triggeredTelescopes.add(telescopeId);

                //add new data using the prefix telescope:<id>:
                String prefix = "telescope:" + telescopeId + ":";
                for (String key : Keys.select(telescopeData, "shower:*,prediction:*,type:*")) {
                    outputItem.put(prefix + key, telescopeData.get(key));
                }
            }

            //add common data stored in one of the old items.
            Data oldItem = items.get(0);
            for (String key : Keys.select(oldItem, "mc:*,array:*,event_id,run_id,unique_event_id,source_file")) {
                outputItem.put(key, oldItem.get(key));
            }
            outputItem.put("array:triggered_telescopes", Ints.toArray(triggeredTelescopes));

            //clear the list of items and add the next on which has already been recieved
            items.clear();
            items.add(inputItem);

            //set the new id to compare new items to
            previousId = id;
            return outputItem;
        }
    }
}
