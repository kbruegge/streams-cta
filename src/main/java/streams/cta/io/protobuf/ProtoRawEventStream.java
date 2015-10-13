package streams.cta.io.protobuf;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * ProtoRawEventStream uses ZeroMQ with a PUB / SUB pattern to subscribe for telescope events
 * serialized with protocol buffer format.
 *
 * @author kai
 */
public class ProtoRawEventStream extends AbstractStream {
    static Logger log = LoggerFactory.getLogger(ProtoRawEventStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    /**
     * Message type is a filter for subscribed messages. Any message with other message type will be
     * skipped.
     */
    byte[] messageType = {10, 2, 84, 82};


    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};

    CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, 1, 0, 0, 0, null, null, null);

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
        subscriber.subscribe(messageType);
    }

    @Override
    public Data readNext() throws Exception {

        // wait for a message with a right type
        byte[] data = subscriber.recv(0);
        try {
            RawCTAEvent.RawEvent rawEvent = RawCTAEvent.RawEvent.parseFrom(data);

            short[][] samples = new short[rawEvent.numPixel][rawEvent.roi];
            for (int pix = 0; pix < rawEvent.numPixel; pix++) {
                for (int slice = 0; slice < rawEvent.roi; slice++) {
                    samples[pix][slice] = (short) rawEvent.samples[(pix * rawEvent.roi + slice)];
                }
            }

            Data item = DataFactory.create();

            item.put("@telescope", telescope);
            item.put("@timestamp", LocalDateTime.now());
            item.put("@raw_data", samples);
            return item;

        } catch (InvalidProtocolBufferNanoException e) {
            log.error("Could not parse the protobuf");
            return null;
        }
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
