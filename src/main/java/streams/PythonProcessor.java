package streams;

import net.razorvine.pyro.PyroProxy;
import stream.*;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test processor calling python code. Somewhere. Dunno yet.
 * Created by kbruegge on 3/16/17.
 */
public class PythonProcessor implements StatefulProcessor{

    @Parameter(required = true, description = "Name of the method to be called")
    public String method;

    private PyroProxy remoteObject;

    @Override
    public Data process(Data item) {
        try {

            HashMap map = (HashMap) remoteObject.call(method, item);
            item.clear();
            item.putAll(map);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Python call failed.");
        }
        return item;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
            remoteObject = (PyroProxy) context.get("pyro_proxy");
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        remoteObject.close();
    }
}
