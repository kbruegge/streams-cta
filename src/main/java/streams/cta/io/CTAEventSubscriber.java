package streams.cta.io;

import org.zeromq.ZMQ;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;


/**
 * Created by kai on 11.08.15.
 */
public class CTAEventSubscriber extends AbstractStream {

    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        //vollmond
        subscriber.connect("tcp://129.217.160.98:5556");
        //phido
        subscriber.connect("tcp://129.217.160.202:5556");
        subscriber.subscribe(new byte[]{1,0,1,0,1});
    }

    @Override
    public Data readNext() throws Exception {
        byte[] data = subscriber.recv(0);
//        System.out.println("Recieved data with length: " + data.length);
        Data item = DataFactory.create();
        item.put("data_bytes", data);
        return item;
    }

    @Override
    public void close() throws Exception {
        super.close();
        subscriber.close();
        context.term();
    }
}
