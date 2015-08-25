package streams.cta.io.capnproto;

import org.capnproto.MessageReader;
import org.capnproto.PrimitiveList;
import org.capnproto.ReaderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;

/**
 * Created by alexey on 20/08/15.
 */
public class CapnProtoObjRawEventStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(CapnProtoObjRawEventStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

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
        subscriber.subscribe(new byte[]{});
    }

    @Override
    public Data readNext() throws Exception {

        ByteBuffer buf = ByteBuffer.allocateDirect(8192);
        subscriber.recvByteBuffer(buf, 0);

        buf.order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<ByteBuffer> list = new ArrayList<>();
        buf.flip();
        list.add(buf);
        while (subscriber.hasReceiveMore()) {
            buf = ByteBuffer.allocate(130000);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            subscriber.recvByteBuffer(buf, 0);
            buf.flip();
            list.add(buf);
        }

        MessageReader message =
                new MessageReader(
                        list.toArray(new ByteBuffer[list.size()]),
                        ReaderOptions.DEFAULT_READER_OPTIONS);

        RawCTAEvent.Event.Reader rawEvent = message.getRoot(RawCTAEvent.Event.factory);
        int numPixel = rawEvent.getNumPixel();
        int roi = rawEvent.getRoi();
        int telescopeId = rawEvent.getTelescopeId();
        String type = rawEvent.getType().toString();

        // Deserialize

        short[][] samples = new short[numPixel][roi];
        PrimitiveList.Short.Reader r = rawEvent.getSamples();
        for (int pix = 0; pix < numPixel; pix++) {
            int pixStart = pix * roi;
            for (int slice = 0; slice < roi; slice++) {
                samples[pix][slice] = r.get(pixStart + slice);
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
