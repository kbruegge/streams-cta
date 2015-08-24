package streams.cta.io.msgpack;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.ByteArrayInputStream;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;

/**
 * Created by alexey on 20/08/15.
 */
public class MsgPackRawEventStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(MsgPackRawEventStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;
    private ByteArrayInputStream in;
    private MessageUnpacker unpacker;

    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
        subscriber.subscribe(new byte[]{-51});
    }

    @Override
    public Data readNext() throws Exception {
        // Deserialize
        in = new ByteArrayInputStream(subscriber.recv());
        unpacker = MessagePack.newDefaultUnpacker(in);

        int numPixel = unpacker.unpackInt();
        int telescopeId = unpacker.unpackInt();
        int roi = unpacker.unpackInt();
        String type = unpacker.unpackString();

        int sampleSize = unpacker.unpackArrayHeader();
        short[][] samples = new short[numPixel][roi];
        boolean stop = false;
        for (int pix = 0; pix < numPixel && !stop; pix++) {
            for (int slice = 0; slice < roi && !stop; slice++) {
                if (unpacker.hasNext()) {
                    samples[pix][slice] = unpacker.unpackShort();
                } else {
                    stop = true;
                }
            }
        }

        Data item = DataFactory.create();
        item.put("@raw_data", samples);
        return item;
    }

    @Override
    public void close() throws Exception {
        super.close();
        subscriber.close();
        context.term();
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
}
