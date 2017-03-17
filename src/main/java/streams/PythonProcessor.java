package streams;

import net.razorvine.pyro.PyroProxy;
import stream.*;
import stream.annotations.Parameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Test processor calling python code. Somewhere. Dunno yet.
 * Created by kbruegge on 3/16/17.
 */
public class PythonProcessor implements StatefulProcessor{

    @Parameter(required = true, description = "Name of the method to be called")
    public String method;

    private PythonBridge bridge;

    @Override
    public Data process(Data item) {
        try {
            @SuppressWarnings("unchecked") HashMap<String, Serializable> map = (HashMap) bridge.callMethod(method, item);
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
            bridge = (PythonBridge) context.get("python_bridge");
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
    }
}
