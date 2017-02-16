package streams.cta.features;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;
import streams.cta.cleaning.TailCut;
import streams.cta.io.ImageStream;
import streams.hexmap.CameraGeometry;

import static org.junit.Assert.assertTrue;
import static streams.cta.io.Names.TRIGGERED_TELESCOPE_IDS;


/**
 * Test calulcation of some hillas parameters calculations.
 * Created by kbruegge on 2/15/17.
 */
public class HillasTest {

    private ImageStream s;
    private WidthLengthDelta hillas;
    private TailCut tailCut;
    private Size size;
    private COG cog;

    @Before
    public void setUp() throws Exception {
        s = new ImageStream();
        s.setUrl(new SourceURL(CameraGeometry.class.getResource("/images.json.gz")));
        s.init();

        tailCut = new TailCut();
        hillas = new WidthLengthDelta();
        cog = new COG();
        size = new Size();

    }

    @After
    public void tearDown() throws Exception {
        s.close();
    }


    /**
     * Create a stream of images. Apply the tailcut and hillas processor and check the output
     * stored in the data item
     */
    @Test
    public void testStreamWithHillas() throws Exception {

        Data data = s.read();
        while (data != null){
            tailCut.process(data);
            size.process(data);
            cog.process(data);
            hillas.process(data);

            int[]  tels = (int[]) data.get(TRIGGERED_TELESCOPE_IDS);
            for(int id : tels){
                assertTrue(
                        "data item does not contain shower width",
                        data.containsKey("telescope:" + id + ":shower:width"));
                assertTrue(
                        "data item does not contain shower length",
                        data.containsKey("telescope:" + id + ":shower:length"));
                assertTrue(
                        "data item does not contain shower delta",
                        data.containsKey("telescope:" + id + ":shower:delta"));


                double width = (double) data.get("telescope:" + id + ":shower:width");
                double length = (double) data.get("telescope:" + id + ":shower:length");

                //length should always be larger or equal to width because of maths!
                assertTrue("Length hast to be greater or equal to width", length >= width);
            }


            data = s.read();
        }

        s.close();
    }
}