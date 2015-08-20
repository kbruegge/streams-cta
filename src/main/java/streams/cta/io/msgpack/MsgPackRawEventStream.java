package streams.cta.io.msgpack;

import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

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

    byte [] messageType = { 10, 2, 84, 82 };

    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        for(String address: addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
        subscriber.subscribe(messageType);
    }

    @Override
    public Data readNext() throws Exception {
        byte[] data = subscriber.recv(0);
        MessagePack msgpack = new MessagePack();

        // Deserialize
        RawCTAEvent rawEvent = msgpack.read(data, RawCTAEvent.class);

        Data item = DataFactory.create();
        item.put("@raw_data", rawEvent.samples);
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
