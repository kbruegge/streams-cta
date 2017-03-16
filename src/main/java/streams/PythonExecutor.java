package streams;

import java.io.IOException;

/**
 * Created by kbruegge on 3/16/17.
 */
public class PythonExecutor implements AutoCloseable {

    private Process p;

    public PythonExecutor(String pathToPythonScript) throws IOException, InterruptedException {
        if (p!=null){
            throw new IllegalStateException("start() was already called on this instance.");
        }

        String[] command = {"python", pathToPythonScript};
        ProcessBuilder pB = new ProcessBuilder(command);
        p = pB.start();

        //TODO: find a way to check whether this has finished loading isntead of sleeping
        Thread.sleep(2000);
    }

    private void stop(){
        if (p != null){
            p.destroy();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
