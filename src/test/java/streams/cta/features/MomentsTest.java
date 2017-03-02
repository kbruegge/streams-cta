package streams.cta.features;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import stream.Data;
import stream.flow.ForEach;
import stream.io.SourceURL;
import streams.MergeByTelescope;
import streams.SplitByTelescope;
import streams.cta.cleaning.TailCut;
import streams.cta.io.ImageStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static streams.cta.io.Names.TRIGGERED_TELESCOPE_IDS;


/**
 * Test calulcation of some hillas parameters calculations. Created by kbruegge on 2/15/17.
 */
public class MomentsTest {

    private ImageStream stream;
    private SplitByTelescope split;
    private ForEach forEach;
    private MergeByTelescope merge;

    final String splitKey = "@telescopes";

    @Before
    public void setUp() throws Exception {
        stream = new ImageStream(new SourceURL(ImageStream.class.getResource("/images.json.gz")));
        stream.init();

        split = new SplitByTelescope();
        split.setKey(splitKey);
        TailCut tailCut = new TailCut();
        Moments hillas = new Moments();
        forEach = new ForEach();
        forEach.setKey(splitKey);
        forEach.add(tailCut);
        forEach.add(hillas);
        merge = new MergeByTelescope();
        merge.setKey(splitKey);
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }


    /**
     * Create a stream of images. Apply the tailcut and hillas processor and check the output stored
     * in the data item
     */
    @Test
    public void testStreamWithHillas() throws Exception {

        Data data = stream.read();
        while (data != null) {
            Data splitData = split.process(data);
            Data foreachData = forEach.process(splitData);
            data = merge.process(foreachData);

            int[] tels = (int[]) data.get(TRIGGERED_TELESCOPE_IDS);
            for (int id : tels) {
                assertTrue(
                        "data item does not contain shower width",
                        data.containsKey("telescope:" + id + ":shower:width"));
                assertTrue(
                        "data item does not contain shower length",
                        data.containsKey("telescope:" + id + ":shower:length"));
                assertTrue(
                        "data item does not contain shower delta",
                        data.containsKey("telescope:" + id + ":shower:delta"));
                assertTrue(
                        "data item does not contain shower delta",
                        data.containsKey("telescope:" + id + ":shower:delta"));

                double delta = (double) data.get("telescope:" + id + ":shower:delta");
                double psi = (double) data.get("telescope:" + id + ":shower:psi");

                assertEquals(delta, psi, 0.0);

                double width = (double) data.get("telescope:" + id + ":shower:width");
                double length = (double) data.get("telescope:" + id + ":shower:length");

                //length should always be larger or equal to width because of maths!
                assertTrue("Length hast to be greater or equal to width", length >= width);
            }


            data = stream.read();
        }

        stream.close();
    }
}