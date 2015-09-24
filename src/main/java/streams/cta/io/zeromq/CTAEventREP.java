package streams.cta.io.zeromq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

/**
 * ZeroMQ server in REP/REQ pattern that listens for requests from a client and sends data if there
 * is an incoming request.
 *
 * @author alexey
 */
public class CTAEventREP extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(CTAEventREP.class);

    private ZMQ.Socket publisher;
    private ZMQ.Context context;

    @Parameter(required = false)
    String[] addresses = {"tcp://*:5556"};

    @Override
    public void init(ProcessContext processContext) throws Exception {
        context = ZMQ.context(1);

        //  Socket to talk to clients
        publisher = context.socket(ZMQ.REP);
        for (String address : addresses) {
            publisher.bind(address);
            log.info("Binding to address: " + address);
        }
    }

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        // Wait for request from the client
        publisher.recv(0);

        ByteBuffer bb = ByteBuffer.allocate(telescope.type.numberOfPixel * eventData[0].length * 2);
        for (short[] arr : eventData) {
            bb.asShortBuffer().put(arr);
        }

        byte[] data = bb.array();
        data[0] = 1;
        data[1] = 0;
        data[2] = 1;
        data[3] = 0;
        data[4] = 1;

        // Send reply back to client
        publisher.send(bb.array(), 0);
        return input;
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
