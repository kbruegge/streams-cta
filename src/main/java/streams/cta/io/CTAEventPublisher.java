package streams.cta.io;

//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import com.esotericsoftware.kryo.Kryo;
import org.zeromq.ZMQ;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;
import java.time.LocalDateTime;


public class CTAEventPublisher extends CTARawDataProcessor implements StatefulProcessor {

    private ZMQ.Socket publisher;
    private ZMQ.Context context;

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {
        //  Prepare our context and publisher
        //  Initialize random number generator

        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(telescope.type.numberOfPixel * eventData[0].length * 2);
        for (short[] arr : eventData) {
            bb.asShortBuffer().put(arr);
        }


//        System.out.println("Sending data and waiting 1 second");
        byte[] data = bb.array();
        data[0] = 1;
        data[1] = 0;
        data[2] = 1;
        publisher.send(bb.array(),0);
        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        context = ZMQ.context(1);

        publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:5556");
        publisher.bind("ipc://weather");
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        publisher.close();
        context.term();
    }
}
