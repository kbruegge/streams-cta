package streams.cta.cleaning;

import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;
import streams.cta.io.ImageStream;
import streams.hexmap.CameraGeometry;
import streams.hexmap.Shower;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Create an imagestream and check whether pixels get actually removed by the cleaning procedure.
 * Created by kbruegge on 2/13/17.
 */
public class CleaningTest {


    @Test
    public void testStreamWithTailCut() throws Exception {
        ImageStream s = new ImageStream(new SourceURL(ImageStream.class.getResource("/images.json.gz")));
        s.init();

        TailCut tc = new TailCut();


        Data data = s.read();
        while (data != null){
            tc.process(data);
            data = s.read();
        }

        s.close();
    }


    @Test
    public void testTailCut() throws Exception {
        ImageStream s = new ImageStream(new SourceURL(ImageStream.class.getResource("/images.json.gz")));
        s.init();

        TailCut tc = new TailCut();

        //get some data and clean it
        Data data = s.read();
        tc.process(data);

        //test that the shower objects are in the data item.
        int[]  tels = (int[]) data.get("triggered_telescopes:ids");
        for(int id : tels){
            assertTrue("data item does not contain shower", data.containsKey("telescope:" + id + ":shower"));

            Shower shower = (Shower) data.get("telescope:" + id + ":shower");

            assertFalse("shower pixels should not be empty", shower.pixels.isEmpty());
            assertTrue("camera id saved in shower has to match the true id", shower.cameraId == id);
        }

        s.close();
    }
}
