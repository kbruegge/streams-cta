package streams;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.Queue;

import java.util.List;
import java.util.Set;

/**
 * split the data items by telescope. Recieves on data item with many telescopes
 * like 'telescope:*:*' and creates a new data item for each telescope.
 * The telescope prefix will be dropped first.
 * For example
 *
 * 'telescope:2:shower:width'
 *
 * will become
 *
 * 'shower:width'
 *
 * Created by kbruegge on 2/17/17.
 */
public class SplitByTelescope implements Processor{

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    @Parameter(required = true, description = "The queue in which to put the new data items" +
            "which were created by splitting the data item given to this processor")
    Queue queue;

    @Override
    public Data process(Data item) {

        int[] triggeredTelescopes = (int[]) item.get("array:triggered_telescopes");

        //create a new data item for each triggered telescope
        for(int id : triggeredTelescopes){
            Data data = DataFactory.create();

            Set<String> selectedKeys = Keys.select(item, "telescope:" + id + ":*");
            for(String key : selectedKeys){

                List<String> splitToList = Splitter.on(":")
                                                    .trimResults()
                                                    .omitEmptyStrings()
                                                    .splitToList(key);

                //remove first two elements (telescope:*:)
                List<String> subList = splitToList.subList(2, splitToList.size());

                String newKey = Joiner.on(":").join(subList);
                data.put(newKey, item.get(key));
            }

            //copy array wide information
            Set<String> arrayWideKeys= Keys.select(item, "array:*");
            for(String key : arrayWideKeys){
                data.put(key, item.get(key));
            }
            data.put("telescope:id", id);

            try {
                queue.write(data.createCopy());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

        }

        return item;
    }
}
