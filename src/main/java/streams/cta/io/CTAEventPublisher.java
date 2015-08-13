package streams.cta.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;
import java.time.LocalDateTime;

/**
 *
 * Created by kai on 11.08.15.
 */
public class CTAEventPublisher extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(CTAEventPublisher.class);

    private ZMQ.Socket publisher;
    private ZMQ.Context context;



    @Parameter(required = false)
    String[] addresses = {"tcp://*:5556"};

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {


        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(telescope.type.numberOfPixel * eventData[0].length * 2);
        for (short[] arr : eventData) {
            bb.asShortBuffer().put(arr);
        }


//        System.out.println("Sending data and waiting 1 second");
        byte[] data = bb.array();
        data[0] = 1;
        data[1] = 0;
        data[2] = 1;
        data[3] = 0;
        data[4] = 1;
        publisher.send(bb.array(),0);
        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        context = ZMQ.context(1);

        publisher = context.socket(ZMQ.PUB);
        for(String address: addresses) {
            publisher.bind(address);
            log.info("Binding to address: " + address);
        }
//        publisher.bind("ipc://cta_data");
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        publisher.close();
        context.term();
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

}
