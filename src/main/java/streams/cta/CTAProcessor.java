package streams.cta;

import stream.Data;
import stream.ProcessContext;
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
        TelescopeEvent telescopeEvent = (TelescopeEvent) input.get("@event");
        return process(input, telescopeEvent);
    }

    public abstract Data process(Data input, TelescopeEvent telescopeEvent);
}
