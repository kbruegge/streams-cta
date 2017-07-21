package streams.python;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.ProcessorList;
import stream.annotations.Parameter;
import stream.io.SourceURL;

/**
 * This can be used as an encapsulating element in a streams xml file in which the PythonProcessor can be called.
 * The context gets an url which points to a python file. The PythonProcessor can the call specific methods
 * on that file.
 *
 * @see PythonProcessor
 *
 * <pre>
 * {@code
 *  <streams.python.PythonContext url="file:./python/processors/test.py">
 *
 *   <streams.python.PythonProcessor method="process" />
 *
 *  </streams.python.PythonContext>
 * }
 * </pre>
 * 
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
