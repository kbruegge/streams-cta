package streams.cta.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.FileInputStream;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Created by kai on 10.08.15.
 */
public class KryoStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(KryoStream.class);

    public KryoStream(){}

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

        input = new Input(url.openStream());
        kryo.register(LocalDateTime.class, new LocalDateTimeSerializer());
    }

    @Override
    public Data readNext() throws Exception {
        if(itemCounter == limit - 1){
            itemCounter = 0;
            count = 0l;
            return null;
        }
        try {
            Data item = DataFactory.create(kryo.readObject(input, map.getClass()));
            itemCounter++;
            //the kryo files contain keys for datarate which have been produced while writing these files.
            item.remove("@datarate");
            return item;
        } catch (KryoException e){
            log.error("Kryo Exception. End of file reached?");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        input.close();
    }
}
