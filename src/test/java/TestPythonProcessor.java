import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;
import stream.io.SourceURL;
import stream.runtime.ProcessContextImpl;
import streams.PythonBridge;
import streams.PythonContext;
import streams.PythonProcessor;

import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test the pyro python bridge
 *
 * https://github.com/irmen/Pyrolite
 *
 * Created by kbruegge on 3/16/17.
 */
public class TestPythonProcessor {

    @Test
    public void testPythonBridge() throws Exception {

        String resource = TestPythonProcessor.class.getResource("/test_interface.py").getPath();

        try(PythonBridge d = new PythonBridge(resource);){

            int result = (int) d.callMethod("add", 1, 1 );

            assertThat(result, is(2));

        }
    }

    @Test
    public void testContext() throws Exception {

        URL resource = TestPythonProcessor.class.getResource("/test_interface.py");

        try(PythonContext c = new PythonContext()) {
            c.url = new SourceURL(resource);

            PythonProcessor p = new PythonProcessor();
            p.method = "process";
            c.add(p);

            c.init(new ProcessContextImpl());

            Data item = DataFactory.create(ImmutableMap.of("thing", 123));
            Data newItem = c.process(item);

            assertThat(item.get("thing"), is(newItem.get("thing")));
        }

    }

    @Test
    public void testPutNewThingInDataItem() throws Exception {

        URL resource = TestPythonProcessor.class.getResource("/test_interface.py");

        try(PythonContext c = new PythonContext()) {
            c.url = new SourceURL(resource);

            PythonProcessor p = new PythonProcessor();
            p.method = "add_thing_to_item";
            c.add(p);

            c.init(new ProcessContextImpl());

            Data item = DataFactory.create();
            Data newItem = c.process(item);

            assertTrue(newItem.containsKey("thing"));
            assertThat(newItem.get("thing"), is("an awesome thing"));
        }

    }
}
