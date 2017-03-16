package streams;

import com.google.common.base.Splitter;
import net.razorvine.pyro.PyroProxy;
import net.razorvine.pyro.PyroURI;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Test processor calling python code. Somewhere. Dunno yet.
 * Created by kbruegge on 3/16/17.
 */
public class PythonProcessor implements StatefulProcessor{

    @Parameter(required = false, description = "The name of the python method to call." +
            "Including the module e.g mymodule.myclass.process")
    public String method;

    private PyroServer server;
    private PyroProxy remoteobject;

    public static class PyroServer{

        private Process p;
        private final String[] command;

        public PyroServer(String pythonInterface){
            command = new String[]{"python", pythonInterface};
            p = null;
        }


        public PyroURI create() throws InterruptedException {

            final PyroURI uri = new PyroURI();
            if (p!=null){
                throw new IllegalStateException("create() was already called on this instance.");
            }
            Runnable r = () -> {
                try {

                    ProcessBuilder pB = new ProcessBuilder(command);
                    p = pB.start();
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String s = stdInput.readLine();

                    if (s == null) throw new IOException();
                    List<String> tokens = Splitter.on(",").trimResults().splitToList(s);
                    synchronized (uri) {
                        uri.objectid = tokens.get(0);
                        uri.host = tokens.get(1);
                        uri.port = Integer.parseInt(tokens.get(2));

                        //uri is set. tell the main thread to stop waiting.
                        uri.notifyAll();
                    }

                    // now wait until the process is stopped (which is never). This will throw a interupted exception
                    // when the main thread is killed. this exception will be caught below.
                    p.waitFor();

                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    if (p != null) {
                        p.destroy();
                    }
                }
            };

            new Thread(r).start();

            //wait for the call to notify in the runnable above.
            synchronized (uri) {
                uri.wait();
            }
            return uri;
        }

        public void stop(){
            if (p != null) {
                p.destroy();
            }
        }
    }

    @Override
    public Data process(Data item) {

        try {
            Object result = remoteobject.call("process", item);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        server = new PyroServer(method);
        remoteobject = new PyroProxy(server.create());
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        remoteobject.close();
        server.stop();
    }
}
