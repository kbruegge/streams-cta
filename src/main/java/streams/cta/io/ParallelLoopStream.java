/**
 *
 */
package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import stream.Data;
import stream.DistributedMultiStream;
import stream.annotations.Parameter;
import stream.io.Stream;
import stream.io.multi.AbstractMultiStream;

/**
 * Loops over a specified amount of events in the inner streams. If the first stream is emptied the
 * next one is read until the specified number of event sis reached.
 *
 * @author chris
 */
public class ParallelLoopStream extends DistributedMultiStream {

    static Logger log = LoggerFactory.getLogger(ParallelLoopStream.class);

    private int numberOfInstances = 1;

    @Parameter(description = "How many events you want to loop over.", defaultValue = "100", required = false)
    int events = 100;

    private LoopStream loopStream;

    /**
     * @see AbstractMultiStream#init()
     */
    @Override
    public void init() throws Exception {
        super.init();

        if (numberOfInstances > 1 && limit > 0) {
            log.info("Limit {}\tnumber of instances {}\tresulting limit {}",
                    limit, numberOfInstances, limit / numberOfInstances);
            limit = limit / numberOfInstances;
        }

        loopStream = new LoopStream();
        loopStream.setLimit(limit);

        for (Map.Entry<String, Stream> streamMap : streams.entrySet()) {
            loopStream.addStream(streamMap.getKey(), streamMap.getValue());
        }
        loopStream.events = events;

        loopStream.init();
    }

    /**
     * @see stream.io.AbstractStream#readNext()
     */
    @Override
    public Data readNext() throws Exception {
        return loopStream.readNext();
    }

    @Override
    public void handleParallelism(int instanceNumber, int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
}