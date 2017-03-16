package streams;

import net.razorvine.pyro.NameServerProxy;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;

/**
 * Test processor calling python code. Somewhere. Dunno yet.
 * Created by kbruegge on 3/16/17.
 */
public class PythonProcess implements StatefulProcessor{


    @Override
    public Data process(Data item) {
        return null;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        NameServerProxy ns = NameServerProxy.locateNS(null);


    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
