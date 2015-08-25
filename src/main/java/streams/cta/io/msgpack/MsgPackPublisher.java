package streams.cta.io.msgpack;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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



        MessagePack msgpack = new MessagePack();

        //
        // Serialization
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker packer = msgpack.newPacker(out);

        // Serialize
        try {
            packer.packInt(numPixel);
            packer.packInt(telescope.telescopeId);
            packer.packInt(roi);
            packer.packString("TR");
            packer.packArrayHeader(roi*numPixel);
            for (int pix = 0; pix < eventData.length; pix++) {
                for (int slice = 0; slice < roi; slice++) {
                    packer.packShort( eventData[pix][slice]);
                }
            }
            packer.close();
        } catch (IOException e) {
            log.error("Writing with packer went wrong.");
        }
        publisher.send(out.toByteArray());
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
