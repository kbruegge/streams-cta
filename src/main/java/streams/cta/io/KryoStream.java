package streams.cta.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.FileInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Created by kai on 10.08.15.
 */
public class KryoStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(KryoStream.class);


    public KryoStream(SourceURL url) {
        super(url);
    }

    long itemCounter = 0;
    Input input;
    Kryo kryo = new Kryo();
    HashMap<String, Serializable> map = new HashMap<>();
    @Override
    public void init() throws Exception {
        super.init();
        input = new Input(new FileInputStream(url.getFile()));
        kryo.register(LocalDateTime.class, new LocalDateTimeSerializer());
    }

    @Override
    public Data readNext() throws Exception {
        Data item = DataFactory.create(kryo.readObject(input, map.getClass()));
        itemCounter++;
        return item;
    }

    @Override
    public void close() throws Exception {
        super.close();
        input.close();
    }
}
