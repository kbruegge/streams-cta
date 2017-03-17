package streams;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This processor calls a method on the script provided by the PythonContext xml element.
 *
 * <pre>
 * {@code
 *  <streams.PythonContext url="file:./python/processors/test.py">
 *
 *   <streams.PythonProcessor method="process" />
 *
 *  </streams.PythonContext>
 * }
 * </pre>
 * @see streams.PythonContext
 *
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
