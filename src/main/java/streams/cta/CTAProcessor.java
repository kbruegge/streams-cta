package streams.cta;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;

/**
 * Created by kai on 02.06.15.
 */
public abstract class CTAProcessor implements StatefulProcessor {

    @Override
    public void init(ProcessContext context) throws Exception {
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    @Override
    public Data process(Data input) {
        CTAEvent ctaEvent = (CTAEvent) input.get("@event");
        return process(input, ctaEvent);
    }

    public abstract Data process(Data input, CTAEvent ctaEvent);
}
