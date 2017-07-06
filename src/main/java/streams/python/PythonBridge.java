package streams.python;

import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This object is the heart of the connection between python and java.
 * The constructor takes the path to a python script. on that python script
 * methods can then be called using the callMethod() function.
 *
 * It encapsulates the PyroLite api https://github.com/irmen/Pyrolite for now.
 * Created by kbruegge on 3/16/17.
 */
public class PythonBridge implements AutoCloseable {
    private Logger log = LoggerFactory.getLogger(PythonBridge.class);

    private Process nameServerProcess = null;
    private Process pythonProcess = null;
    private final PyroProxy remoteObject;

    /**
     * The PythonBridge starts a pyro name server by invoking  "python -m Pyro4.naming"
     * using javas ProcessBuilder. The output of that process is parsed for the string "NS Running"
     * until a timeout is reached.
     * After the name server has been started, the actual python script is run.
     * The python script is started in the same manner.
     * The python script has to start the pyro daemon and must not redirect its output to somewhere else.
     *
     * @param pathToPythonScript the path to the python script with the exposed pyro methods
     * @throws IOException in case an error occurs when starting the processes
     */
    public PythonBridge(String pathToPythonScript) throws IOException {
        if (!new File(pathToPythonScript).canRead()){
            log.error("File at " + pathToPythonScript + " is not readable.");
            throw new IOException("Python file not readable");
        }
        try {
            String[] nameServerCommand = {"python", "-u", "-m", "Pyro4.naming"}; // -u for unbuffered python output

            nameServerProcess = new ProcessBuilder(nameServerCommand).start();
            BufferedReader stdin = new BufferedReader(new InputStreamReader(nameServerProcess.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(nameServerProcess.getErrorStream()));
            boolean isNameServerRunning = false;
            for (int i = 0; i < 50; i++) {
                String output = stdin.readLine();
                if (output != null && output.startsWith("NS running")) {
                    log.debug("NameServer is running");
                    isNameServerRunning = true;
                    break;
                }
                Thread.sleep(200);
            }
            if (!isNameServerRunning) {
                stderr.lines().forEach(e -> log.error(e));
                nameServerProcess.destroy();
                throw new IOException("Timeout while waiting for start of NameServer.");
            }


            String[] command = {"python", "-u", pathToPythonScript}; // -u for unbuffered python output

            pythonProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            stdin = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            stderr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));

            boolean isPyroDaemonRunning = false;
            for (int i = 0; i < 50; i++) {
                String output = stdin.readLine();
                if (output != null && output.startsWith("Pyro daemon running.")) {
                    log.debug("Pyro daemon is running.");
                    isPyroDaemonRunning = true;
                    break;
                }
                Thread.sleep(200);
            }
            if (!isPyroDaemonRunning) {
                stderr.lines().forEach(e -> log.error(e));
                pythonProcess.destroy();
                throw new IOException("Timeout while waiting for start of PyroDaemon.");
            }


            NameServerProxy nsProxy = NameServerProxy.locateNS("localhost");

            remoteObject = new PyroProxy(nsProxy.lookup("streams.processors"));
        } catch (IOException | InterruptedException e){
            pythonProcess.destroyForcibly();
            nameServerProcess.destroyForcibly();
            e.printStackTrace();
            throw new IOException("An error occured while invoking the python processes");
        }

    }

    private void stop(){
        remoteObject.close();
        nameServerProcess.destroy();
        pythonProcess.destroy();
    }

    /**
     * Calls a Python method by name.
     * This methods delegates to the {@link PyroProxy#call} method.
     * There is some more code in here which logs python output to the appropriate java log.
     *
     * @param name the name of the python method to call
     * @param args the argumeents to pass to python
     * @return the return value of the python method. The object has to be cast back to the proper type.
     * @throws IOException in case an error occurs when calling the python method.
     */
    public Object callMethod(String name, Object... args) throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));

        Object result = remoteObject.call(name, args);

        logPythonOutput(stdin);

        logPythonOutput(stderr);

        return result;
    }

    /**
     * Read the standard input and error from Python and log it.
     * @param reader buffered reader
     */
    private void logPythonOutput(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            sb.append((char) reader.read());
        }
        String output = sb.toString();
        if (!output.isEmpty()) {
            log.info(output);
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
