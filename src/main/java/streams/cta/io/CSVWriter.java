package streams.cta.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import stream.Data;
import stream.Keys;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * KryoWriter writes DataItem as an Kryo object to a given file.
 *
 * @author kai
 */
public class CSVWriter implements StatefulProcessor {


    @Parameter(required = true, description = "The url to write to")
    SourceURL url;

    @Parameter(required = true)
    Keys keys;


    boolean headerWritten = false;
    private PrintWriter writer;
    String seperator = ",";


    @Override
    public void init(ProcessContext processContext) throws Exception {
        writer = new PrintWriter(new FileOutputStream(url.getFile()));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        writer.flush();
        writer.close();
    }

    @Override
    public Data process(Data data) {
        Set<String> selectedKeys = keys.select(data);
        if (!headerWritten){
            String header = Joiner.on(seperator).join(selectedKeys);
            writer.print('#');
            writer.println(header);
            headerWritten = true;
        }

        List<String> values = selectedKeys.stream()
                                          .map(data::get)
                                          .map(String::valueOf)
                                          .collect(Collectors.toList());

        String joinedValues = Joiner.on(",").join(values);
        writer.println(joinedValues);

        return data;
    }
}
