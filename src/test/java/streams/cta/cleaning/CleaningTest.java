package streams.cta.cleaning;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;
import streams.cta.SplitByTelescope;
import streams.cta.io.ImageStream;
import streams.hexmap.Shower;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Create an imagestream and check whether pixels get actually removed by the cleaning procedure.
 * Created by kbruegge on 2/13/17.
 */
public class CleaningTest {

    ImageStream stream;
    TailCut tailCut;
    SplitByTelescope split;
    final String splitKey = "@telescopes";

    @Before
    public void setUp() throws Exception {
        stream = new ImageStream(new SourceURL(ImageStream.class.getResource("/images.json.gz")));
        stream.init();

        split = new SplitByTelescope();
        split.setKey(splitKey);
        tailCut = new TailCut();
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }

    @Test
    public void testStreamWithTailCut() throws Exception {
        Data data = stream.read();
        while (data != null) {
            Data splitData = split.process(data);
            for (Data tel : (Data[]) splitData.get(splitKey)) {
                Data tailData = tailCut.process(tel);
                assertTrue("Data item does not contain number of pixel in the shower",
                        tailData.containsKey("shower:number_of_pixel"));
            }
            data = stream.read();
        }
        stream.close();
    }


    @Test
    public void testTailCut() throws Exception {
        //get some data and clean it
        Data data = stream.read();
        Data splitData = split.process(data);

        //test that the shower objects are in the data item.
        Data[] telArray = (Data[]) splitData.get(splitKey);
        for (Data tel : telArray) {
            tailCut.process(tel);
            assertTrue("data item does not contain shower", tel.containsKey("shower"));

            Shower shower = (Shower) tel.get("shower");

            assertFalse("shower pixels should not be empty", shower.pixels.isEmpty());

            int id = (int) tel.get("telescope:id");
            assertTrue("camera id saved in shower has to match the true id", shower.cameraId == id);
        }

        stream.close();
    }
}
