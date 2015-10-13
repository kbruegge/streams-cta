package streams.cta.io.protobuf;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * Created by kai on 11.08.15.
 */
public class ProtoEventPublisher extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(ProtoEventPublisher.class);

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

        RawCTAEvent.RawEvent rawEvent = new RawCTAEvent.RawEvent();
        rawEvent.numPixel = numPixel;
        rawEvent.telescopeId = telescope.telescopeId;
        rawEvent.roi = roi;
        rawEvent.messageType = "TR";

        int[] samples = new int[numPixel * roi];
        for (int pix = 0; pix < eventData.length; pix++) {
            int pixStart = pix * roi;
            for (int slice = 0; slice < roi; slice++) {
                samples[pixStart + slice] = eventData[pix][slice];
            }
        }
        rawEvent.samples = samples;

        byte[] bytes =  RawCTAEvent.RawEvent.toByteArray(rawEvent);
        input.put("@packetSize", bytes.length);
        publisher.send(bytes,0);

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

        System.out.println("Sleeping for 0.2 seconds");
        Thread.sleep(200);


        if(publisher != null) {
            publisher.close();
        }
        if(context != null) {
            context.term();
        }

    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

}
