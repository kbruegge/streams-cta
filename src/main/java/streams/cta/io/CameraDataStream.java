package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;

/**
 * CameraServerStream receives raw bytes from a ZMQ.Pull port.
 *
 * @author chris, kai
 */
public class CameraDataStream extends AbstractStream {
    static Logger log = LoggerFactory.getLogger(CameraDataStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    @Parameter(required = false)
    String[] addresses = { "tcp://129.217.160.202:5556" };

    long bytes = 0L;
    long start = 0L;
    long events = 0L;

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.PULL);
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
    }

    @Override
    public Data readNext() throws Exception {
        // wait for a message with a right type
        // log.debug("subscriber.recv(0)");
        byte[] data = subscriber.recv(0);
        if (data.length == 0) {
            return null;
        }

        long now = System.currentTimeMillis();

        if (bytes == 0) {
            start = now;
        }

        events++;
        bytes += data.length;

        if (events == 10000) {
            events = 0;
            Double gbits = (bytes * 8.0) / 1024.0 / 1024.0 / 1024.0;
            Double seconds = (now - start) / 1000.0;
            log.debug("{} read, data rate is {} GBit/s", bytes, gbits / seconds);
        }

        Data item = DataFactory.create();
        item.put("@data", data);
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
