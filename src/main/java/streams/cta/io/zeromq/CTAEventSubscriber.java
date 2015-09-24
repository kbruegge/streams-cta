package streams.cta.io.zeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;


/**
 * ZeroMQ subscriber in PUB/SUB pattern that waits for events from a publisher to which it
 * subscribes.
 *
 * @author kai
 */
public class CTAEventSubscriber extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(CTAEventSubscriber.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};


    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        //vollmond
//        subscriber.connect("tcp://129.217.160.98:5556");
        //phido
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
        subscriber.subscribe(new byte[]{1, 0, 1, 0, 1});
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

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
}
