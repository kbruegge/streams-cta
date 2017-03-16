import net.razorvine.pyro.PyroProxy;
import org.junit.Test;
import streams.PyroNameServer;
import streams.PythonExecutor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test the pyro python bridge
 *
 * https://github.com/irmen/Pyrolite
 *
 * Created by kbruegge on 3/16/17.
 */
public class TestPythonProcessor {

    @Test
    public void testNameServer() throws Exception {

        String resource = TestPythonProcessor.class.getResource("/test_interface.py").getPath();

        try(PythonExecutor p = new PythonExecutor(resource);
                PyroNameServer d = new PyroNameServer()){

            PyroProxy remoteObject = d.lookup("streams.processors");

            int result = (int) remoteObject.call("add", 1, 1 );

            assertThat(result, is(2));

            remoteObject.close();
        }
    }
}
