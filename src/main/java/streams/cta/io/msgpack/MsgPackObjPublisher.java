package streams.cta.io.msgpack;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.IOException;
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
public class MsgPackObjPublisher extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(MsgPackObjPublisher.class);

    private ZMQ.Socket publisher;
    private ZMQ.Context context;
    private ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

    @Parameter(required = false)
    String[] addresses = {"tcp://*:5556"};

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {
        if (telescope.type != CTATelescopeType.LST) {
            log.debug("Found non LST event");
            return null;
        }

        int roi = eventData[0].length;
        int numPixel = telescope.type.numberOfPixel;

        RawCTAEvent rawEvent = new RawCTAEvent();
        rawEvent.numPixel = numPixel;
        rawEvent.telescopeId = telescope.telescopeId;
        rawEvent.roi = roi;
        rawEvent.messageType = "TR";

        short[] samples = new short[numPixel * roi];
        for (int pix = 0; pix < eventData.length; pix++) {
            System.arraycopy(eventData[pix], 0, samples, pix * roi, roi);
        }

        rawEvent.samples = samples;

        try {
            byte[] data = objectMapper.writeValueAsBytes(rawEvent);
            publisher.send(data);
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
