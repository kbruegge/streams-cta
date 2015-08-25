package streams.cta.io.msgpack;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

import java.time.LocalDateTime;

/**
 * Created by alexey on 20/08/15.
 */
public class MsgPackObjRawEventStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(MsgPackObjRawEventStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    private RawCTAEvent rawEvent;
    private ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

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
        subscriber.subscribe(new byte[]{-123});
    }

    @Override
    public Data readNext() throws Exception {
        byte[] data = subscriber.recv();

        // Deserialize
        rawEvent = objectMapper.readValue(data, RawCTAEvent.class);

        short[][] samples = new short[rawEvent.numPixel][rawEvent.roi];
        for (int pix = 0; pix < rawEvent.numPixel; pix++) {
            System.arraycopy(rawEvent.samples, pix * rawEvent.roi, samples[pix], 0, rawEvent.roi);
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
