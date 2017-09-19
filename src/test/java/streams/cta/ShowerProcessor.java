package streams.cta;

import org.junit.Test;
import stream.Data;
import stream.Processor;
import stream.data.DataFactory;
import streams.hexmap.Shower;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static streams.cta.io.Names.TRIGGERED_TELESCOPE_IDS;

/**
 * Test whether the showerprocessor finds the right data in the event item.
 * Created by kbruegge on 2/15/17.
 */
public class ShowerProcessor {

    private class ExpectEmptyShowerProcessor extends CTACleanedDataProcessor{
        @Override
        public Data process(Data input, Shower shower) {
            assertTrue(shower == null);
            return input;
        }
    }

    private class ExpectShowerProcessor extends CTACleanedDataProcessor{
        @Override
        public Data process(Data input, Shower shower) {
            assertFalse(shower != null);
            return input;
        }
    }

    @Test
    public void testEmptyShower() throws Exception {
        Data data = DataFactory.create();
        data.put(TRIGGERED_TELESCOPE_IDS, new int[]{1});

        Processor emptyProcessor = new ExpectEmptyShowerProcessor();
        emptyProcessor.process(data);

        data.put("telescope:1:shower", null);
        emptyProcessor.process(data);
    }

    @Test
    public void testShower() throws Exception {
        Data data = DataFactory.create();
        data.put(TRIGGERED_TELESCOPE_IDS, new int[]{1});
        data.put("telescope:1:shower", new Shower(1));

        Processor processor = new ExpectShowerProcessor();
        processor.process(data);
    }
}
