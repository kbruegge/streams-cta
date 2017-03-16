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
public class PythonContext extends ProcessorList{

    @Parameter(required = true, description = "The path to the python file whose methods should be called")
    public SourceURL url;

    private PyroProxy remoteObject;
    private PythonExecutor pythonExecutor;
    private PyroNameServer nameServer;

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

        pythonExecutor= new PythonExecutor(url.getPath());
        nameServer = new PyroNameServer();


        remoteObject = nameServer.lookup("streams.processors");
        context.set("pyro_proxy", remoteObject);


        super.init(context);
    }

    @Override
    public void finish() throws Exception {
        nameServer.close();
        remoteObject.close();
        remoteObject.close();
    }
}
