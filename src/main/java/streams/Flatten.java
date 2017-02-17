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
 * Created by kbruegge on 2/17/17.
 */
public class Flatten implements Processor{

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    @Parameter(required = true)
    Queue queue;



    @Override
    public Data process(Data item) {

        int[] triggeredTelescopes = (int[]) item.get("array:triggered_telescopes");

        for(int id : triggeredTelescopes){
            Data data = DataFactory.create();

            Set<String> selectedKeys = Keys.select(item, "telescope:" + id + ":*:*");
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
