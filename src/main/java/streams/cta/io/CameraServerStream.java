package streams.cta.io;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;
import streams.cta.io.datamodel.nano.CoreMessages;
import streams.cta.io.datamodel.nano.L0;


import java.time.LocalDateTime;

/**
 * CameraServerStream uses ZeroMQ to subscribe to telescope events
 * serialized with protocol buffer format according to the datamodel as specified by
 * ACTL
 *
 * @author kai
 */
public class CameraServerStream extends AbstractStream {
    static Logger log = LoggerFactory.getLogger(CameraServerStream.class);
    private ZMQ.Context context;
    private ZMQ.Socket subscriber;

    CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, 1, 0, 0, 0, null, null, null);

    @Parameter(required = false)
    String[] addresses = {"tcp://129.217.160.202:5556"};

    @Override
    public void init() throws Exception {
        super.init();
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.PULL);
        for (String address : addresses) {
            log.info("Connecting to address: " + address);
            subscriber.connect(address);
        }
        //subscriber.subscribe(messageType);
    }

    @Override
    public Data readNext() throws Exception {

        // wait for a message with a right type
        byte[] data = subscriber.recv(0);
        try {
            //RawCTAEvent.RawEvent rawEvent = RawCTAEvent.RawEvent.parseFrom(data);
            CoreMessages.CTAMessage ctaMessage = CoreMessages.CTAMessage.parseFrom(data);
            //the message can contain many types and payloads. so far this has not been implemented to my knowledge
            int payloadType = ctaMessage.payloadType[0];
            if(payloadType != CoreMessages.CAMERA_EVENT){
                throw new NotImplementedException("This has only been implemented for Camera Events so far. " +
                        "See data model");
            }
            byte[] payloadData = ctaMessage.payloadData[0];
            L0.CameraEvent cameraEvent = L0.CameraEvent.parseFrom(payloadData);
            //int numPixel = cameraEvent.geometry.pixels.length;
            //int roi = cameraEvent.head.numTraces;
//            log.info(cameraEvent.toString());
//            log.info(cameraEvent.hiGain.toString());
//            log.info(cameraEvent.hiGain.waveforms.toString());
//            log.info(cameraEvent.hiGain.waveforms.samples.toString());
            int format = cameraEvent.hiGain.waveforms.samples.currentComp;
            int datatype = cameraEvent.hiGain.waveforms.samples.type;
            int roi = cameraEvent.hiGain.waveforms.numSamples;
            byte[] hiGainSamples = cameraEvent.hiGain.waveforms.samples.data;

            //log.info("Roi : " + roi);
            //log.info("type : " + datatype);

            if (format != CoreMessages.AnyArray.RAW){
                throw new NotImplementedException("Data compression handling not implemented yet.");
            }
            Data item = DataFactory.create();
            item.put("@telescope", telescope );
            item.put("@timestamp", LocalDateTime.now());


            if(datatype == CoreMessages.AnyArray.S16 ){
                int skip = 2; //datatype has 2 bytes
                int numPixel = (hiGainSamples.length/skip)/roi;
                int counter = 0;
                short[][] samples = new short[numPixel][roi];
                for (int pix = 0; pix < numPixel; pix++) {
                    for (int slice = 0; slice < roi; slice++) {
                        samples[pix][slice] = (short) hiGainSamples[counter*skip];
                        counter++;
                    }
                }
                item.put("@raw_data", samples);
            }

            return item;

        } catch (InvalidProtocolBufferNanoException e) {
            log.error("Could not parse the protobuf");
            return null;
        }
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
