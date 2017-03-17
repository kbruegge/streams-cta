package streams;

import net.razorvine.pyro.PyroProxy;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.ProcessorList;
import stream.annotations.Parameter;
import stream.io.SourceURL;

/**
 * Test processor calling python code. Somewhere. Dunno yet.
 * Created by kbruegge on 3/16/17.
 */
public class PythonContext extends ProcessorList implements AutoCloseable{

    @Parameter(required = true, description = "The path to the python file whose methods should be called")
    public SourceURL url;

    private PythonBridge bridge;

    @Override
    public Data process(Data item) {
        if (item != null){
            for(Processor p : super.processors){
                item = p.process(item);
                if(item == null){
                    return null;
                }
            }
        }
        return item;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

        bridge = new PythonBridge(url.getPath());

        context.set("python_bridge", bridge);

        super.init(context);
    }

    @Override
    public void finish() throws Exception {
        bridge.close();
    }

    @Override
    public void close() throws Exception {
        this.finish();
    }
}
