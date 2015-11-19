package streams.cta.io;


import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;
import streams.cta.io.datamodel.nano.CoreMessages;
import streams.cta.io.datamodel.nano.L0;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * DaqMonitor uses publishes protobufs via zeromq to the daq monitoring component
 *
 * @author kai
 */
public class DaqMonitor extends CTARawDataProcessor implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(DaqMonitor.class);

    private ZMQ.Socket monitorPublisher;
    private ZMQ.Context context;

    private Stopwatch stopwatch;


    @Parameter(required = false, description = "The IP of the daq monitor. Asin in tcp://127.0.0.1:1222")
    String monitorAddress = "tcp://127.0.0.1:4849";


    @Override
    public void init(ProcessContext processContext) throws Exception {
        context = ZMQ.context(1);

        monitorPublisher = context.socket(ZMQ.PUB);
        monitorPublisher.bind(monitorAddress);
        log.info("Binding monitor to address: " + monitorAddress);

        stopwatch = Stopwatch.createStarted();
    }

    private void sendToMonitor(int bytesSend, int elapsedMicros){
        CoreMessages.ThroughputStats stats = new CoreMessages.ThroughputStats();
        stats.comment = "Bytes Published";
        stats.numBytes = bytesSend;
        stats.elapsedUs = elapsedMicros;
        byte[] bytesTosend = CoreMessages.ThroughputStats.toByteArray(stats);
        monitorPublisher.send(bytesTosend);
    }

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, short[][] eventData) {

        //send stuff to the central daq monitor.
        int elapsedMicros = (int) stopwatch.elapsed(TimeUnit.MICROSECONDS);
        stopwatch.reset();
        if (input.get("@packetsize") != null){
            int packetSize = (int) input.get("@packetsize");
            sendToMonitor(packetSize, elapsedMicros);
        }

        return input;
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

        System.out.println("Sleeping for 0.2 seconds");
        Thread.sleep(200);


        if (monitorPublisher != null) {
            monitorPublisher.close();
        }
        if (context != null) {
            context.term();
        }

    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }

}
