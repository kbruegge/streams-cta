package streams;

import net.razorvine.pyro.PyroURI;

/**
 * Created by kbruegge on 3/16/17.
 */
public class Pyro {

    private Process p;
    private final String[] command = {"python", "-m", "Pyro4.naming"};

    
    public void start() throws InterruptedException {
        if (p!=null){
            throw new IllegalStateException("start() was already called on this instance.");
        }

        Runnable r = () -> {
            try {

                ProcessBuilder pB = new ProcessBuilder(command);
                p = pB.start();

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
    }

}
