package streams.cta.io.zeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;


/**
 * ZeroMQ client in REP/REQ pattern that connects to a publisher and send requests for new items to
 * be sent. After an item is processed the next request is sent.
 */
public class CTAEventREQ extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(CTAEventREQ.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.REQ);
        //vollmond
//        subscriber.connect("tcp://129.217.160.98:5556");
        //phido
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
    }

    @Override
    public Data readNext() throws Exception {
        // send request to the publisher
        subscriber.send("Hello".getBytes(), 0);

        // wait for responce from publisher
        byte[] data = subscriber.recv(0);

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
