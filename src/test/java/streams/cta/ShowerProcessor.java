package streams.cta;

import org.junit.Test;
import stream.Data;
import stream.Processor;
import stream.data.DataFactory;
import streams.hexmap.Shower;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by kbruegge on 2/15/17.
 */
public class ShowerProcessor {

    private class ExpectEmptyShowerProcessor extends CTACleanedDataProcessor{
        @Override
        public Data process(Data input, HashMap<Integer, Shower> shower) {
            assertTrue(shower.isEmpty());
            return input;
        }
    }

    private class ExpectShowerProcessor extends CTACleanedDataProcessor{
        @Override
        public Data process(Data input, HashMap<Integer, Shower> shower) {
            assertFalse(shower.isEmpty());
            return input;
        }
    }

    private class ExpectFiveShowersProcessor extends CTACleanedDataProcessor{
        @Override
        public Data process(Data input, HashMap<Integer, Shower> shower) {
            assertFalse(shower.isEmpty());
            assertEquals("added 5 showers to the data item. all should be in the map",
                     5, shower.size());
            return input;
        }
    }

    @Test
    public void testEmptyShower() throws Exception {
        Data data = DataFactory.create();
        data.put("triggered_telescopes:ids", new int[]{1});

        Processor emptyProcessor = new ExpectEmptyShowerProcessor();
        emptyProcessor.process(data);

        data.put("telescope:1:shower", null);
        emptyProcessor.process(data);
    }

    @Test
    public void testShower() throws Exception {
        Data data = DataFactory.create();
        data.put("triggered_telescopes:ids", new int[]{1});
        data.put("telescope:1:shower", new Shower(1));

        Processor processor = new ExpectShowerProcessor();
        processor.process(data);
    }


    @Test
    public void testManyShowers() throws Exception {
        Data data = DataFactory.create();
        data.put("triggered_telescopes:ids", new int[]{1,2,3,4,5});
        data.put("telescope:1:shower", new Shower(1));
        data.put("telescope:2:shower", new Shower(1));
        data.put("telescope:3:shower", new Shower(1));
        data.put("telescope:4:shower", new Shower(1));
        data.put("telescope:5:shower", new Shower(1));

        Processor processor = new ExpectFiveShowersProcessor();
        processor.process(data);
    }
}
