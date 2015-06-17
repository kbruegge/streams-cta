/**
 *
 */
package streams.cta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;

/**
 * @author chris
 */
public class Throughput extends AbstractProcessor {

    static Logger log = LoggerFactory.getLogger(Throughput.class);

    long pixelCount = 0L;


    @Override
    public void init(ProcessContext ctx) throws Exception {
        super.init(ctx);
    }

    /**
     * @see stream.Processor#process(stream.Data)
     */
    @Override
    public Data process(Data input) {
        CTAEvent ev = (CTAEvent) input.get("@event");
        pixelCount += ev.numberOfPixels;
        return input;
    }

    /**
     * @see stream.AbstractProcessor#finish()
     */
    @Override
    public void finish() throws Exception {
        super.finish();
    }

}
