package streams.cta.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * KryoWriter writes DataItem as an Kryo object to a given file.
 *
 * @author kai
 */
public class KryoWriter implements StatefulProcessor {


    @Parameter(required = true, description = "The url to write to")
    SourceURL url;

    Kryo kryo;
    Output output;

    @Override
    public void init(ProcessContext processContext) throws Exception {
        output = new Output(new FileOutputStream(url.getFile()));
        kryo = new Kryo();
        kryo.register(LocalDateTime.class, new LocalDateTimeSerializer());
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        output.close();
    }

    @Override
    public Data process(Data data) {
        HashMap<String, Serializable> map = (HashMap<String, Serializable>) data.createCopy();
        kryo.writeObject(output, map);
        return data;
    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }
}
