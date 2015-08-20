package streams.cta.io.msgpack;

import org.msgpack.MessagePack;
import org.msgpack.template.ShortArrayTemplate;
import org.msgpack.template.TemplateRegistry;
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
public class MsgPackPublisher extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(MsgPackPublisher.class);

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

        int roi = eventData[0].length;
        int numPixel = telescope.type.numberOfPixel;


        RawCTAEvent rawEvent = new RawCTAEvent();
        rawEvent.numPixel = numPixel;
        rawEvent.telescopeId = telescope.telescopeId;
        rawEvent.roi = roi;
        rawEvent.messageType = "TR";

        short[] samples = new short[numPixel * roi];
        for (int pix = 0; pix < eventData.length; pix++) {
            for (int slice = 0; slice < roi; slice++) {
                samples[pix * roi + slice] = eventData[pix][slice];
            }
        }
        rawEvent.samples = samples;

        MessagePack msgpack = new MessagePack();
        // Serialize
        try {
            byte[] bytes = msgpack.write(rawEvent);
            publisher.send(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        TemplateRegistry t = new TemplateRegistry(null);
        t.register(short[].class, ShortArrayTemplate.getInstance());
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
}
