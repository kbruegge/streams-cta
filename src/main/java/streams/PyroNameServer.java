package streams;

import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroProxy;

import java.io.IOException;

/**
 * Created by kbruegge on 3/16/17.
 */
public class PyroNameServer implements AutoCloseable {

    private final NameServerProxy nsProxy;
    private final Process p;


    public PyroNameServer()throws IOException, InterruptedException {
        String[] command = {"python", "-m", "Pyro4.naming"};
        ProcessBuilder pB = new ProcessBuilder(command);
        p = pB.start();

        Thread.sleep(2000);

        nsProxy = NameServerProxy.locateNS("localhost");
    }

    private void stop(){
        if (p != null){
            p.destroy();
        }
    }

    public PyroProxy lookup(String name) throws IOException {
        return new PyroProxy(nsProxy.lookup(name));
    }
    @Override
    public void close() throws Exception {
        stop();
    }
}
