package streams.cta.io.capnproto;

import org.capnproto.MessageReader;
import org.capnproto.PrimitiveList;
import org.capnproto.SerializePacked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * Created by alexey on 20/08/15.
 */
public class CapnProtoObjRawEventStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(CapnProtoObjRawEventStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, 1, 0, 0, 0, null, null, null);


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

        byte[] bytes = subscriber.recv();

        // buffered version
        //MessageReader message = SerializePacked.read(
        //        new BufferedInputStreamWrapper(new DynamicArrayInputStream(ByteBuffer.wrap(bytes))));

        // unbuffered version
        MessageReader message = SerializePacked.readFromUnbuffered(
                new DynamicArrayInputStream(ByteBuffer.wrap(bytes)));

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
        item.put("@telescope", telescope);
        item.put("@timestamp", LocalDateTime.now());
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
