package streams.cta.io.capnproto;

import org.apache.storm.netty.buffer.ChannelBuffer;
import org.capnproto.MessageBuilder;
import org.capnproto.PrimitiveList;
import org.capnproto.Serialize;
import org.capnproto.SerializePacked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * Created by alexey on 20/08/15.
 */
public class CapnProtoObjPublisher extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(CapnProtoObjPublisher.class);

    private ZMQ.Socket publisher;
    private ZMQ.Context context;

    @Parameter(required = false)
    String[] addresses = {"tcp://*:5556"};

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {
        if (telescope.type != CTATelescopeType.LST) {
            log.debug("Found non LST event");
            return null;
        }

        MessageBuilder message = new MessageBuilder();

        RawCTAEvent.Event.Builder rawEvent = message.initRoot(RawCTAEvent.Event.factory);

        int roi = eventData[0].length;
        int numPixel = telescope.type.numberOfPixel;

        rawEvent.setNumPixel(numPixel);
        rawEvent.setTelescopeId(telescope.telescopeId);
        rawEvent.setRoi(roi);
        rawEvent.setType("TR");

        PrimitiveList.Short.Builder samples = rawEvent.initSamples(numPixel * roi);
        for (int pix = 0; pix < numPixel; pix++) {
            int pixStart = pix * roi;
            for (int slice = 0; slice < roi; slice++) {
                samples.set(pixStart + slice, eventData[pix][slice]);
            }
        }

        try {
            DynamicArrayOutputStream daos = new DynamicArrayOutputStream(50000);
            SerializePacked.write(daos, message);
            ByteBuffer buf = daos.getWriteBuffer();
            buf.flip();
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);
            publisher.send(bytes, 0);
            input.put("@packetSize", bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        context = ZMQ.context(1);
        publisher = context.socket(ZMQ.PUB);
        for (String address : addresses) {
            publisher.bind(address);
            log.info("Binding to address: " + address);
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        System.out.println("Sleeping for 4 seconds");
        Thread.sleep(4000);
        if (publisher != null) {
            publisher.close();
        }
        if (context != null) {
            context.term();
        }
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
}
