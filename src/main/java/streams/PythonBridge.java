package streams;

import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroProxy;

import java.io.IOException;

/**
 * This object is the heart of the connection between python and java.
 * The constructor takes the path to a python script. on that python script
 * methods can then be called using the callMethod() function.
 *
 * It encapsulates the PyroLite api https://github.com/irmen/Pyrolite for now.
 * Created by kbruegge on 3/16/17.
 */
public class PythonBridge implements AutoCloseable {

    private final Process nameServerProcess;
    private final Process pythonProcess;
    private final PyroProxy remoteObject;


    public PythonBridge(String pathToPythonScript) throws IOException, InterruptedException {
        String[] nameServerCommand = {"python", "-m", "Pyro4.naming"};
        ProcessBuilder pB = new ProcessBuilder(nameServerCommand);
        nameServerProcess = pB.start();

        //TODO: find a way to check whether this has finished loading instead of sleeping
        Thread.sleep(2000);


        String[] command = {"python", pathToPythonScript};
        pythonProcess = new ProcessBuilder(command).start();

        Thread.sleep(2000);

        NameServerProxy nsProxy = NameServerProxy.locateNS("localhost");

        remoteObject = new PyroProxy(nsProxy.lookup("streams.processors"));
    }

    private void stop(){
        remoteObject.close();
        nameServerProcess.destroy();
        pythonProcess.destroy();
    }

    public Object callMethod(String name, Object... args) throws IOException {
        return remoteObject.call(name, args);
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
