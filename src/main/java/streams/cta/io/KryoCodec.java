/**
 * 
 */
package streams.cta.io;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stream.Data;
import stream.data.DataFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a data item codec using an internal Kryo instance. The
 * kryo instance is shared, so the calls to decode and encode are <b>not</b>
 * thread-safe.
 * 
 * @author Christian Bockermann
 *
 */
public class KryoCodec implements streams.codec.Codec<Data> {

    final HashMap<String, Serializable> template = new HashMap<String, Serializable>();
    final com.esotericsoftware.kryo.Kryo codec = new com.esotericsoftware.kryo.Kryo();

    public KryoCodec() {
        codec.register(LocalDateTime.class, new LocalDateTimeSerializer());
    }

    /**
     * @see stream.io.Codec#decode(byte[])
     */
    @Override
    public Data decode(byte[] rawBytes) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> values = codec.readObject(new Input(new ByteArrayInputStream(rawBytes)),
                template.getClass());
        Data item = DataFactory.create(values);
        return item;
    }

    /**
     * @see stream.io.Codec#encode(java.lang.Object)
     */
    @Override
    public byte[] encode(Data object) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output out = new Output(baos);
        codec.writeObject(out, object);
        out.flush();
        out.close();
        return baos.toByteArray();
    }
}
