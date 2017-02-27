package streams.cta.stereo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;
import streams.cta.cleaning.TailCut;
import streams.cta.features.COG;
import streams.cta.features.Moments;
import streams.cta.features.Size;
import streams.cta.features.WidthLengthDelta;
import streams.cta.io.ImageStream;
import streams.hexmap.CameraGeometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static streams.cta.io.Names.TRIGGERED_TELESCOPE_IDS;

/**
 * Test the stereo features processor.
 *
 * Created by kbruegge on 2/27/17.
 */
public class StereoTest {


    private ImageStream s;
    private Moments hillas;
    private TailCut tailCut;
    private Stereo stereo;

    @Before
    public void setUp() throws Exception {
        s = new ImageStream();
        s.setUrl(new SourceURL(CameraGeometry.class.getResource("/images.json.gz")));
        s.init();

        tailCut = new TailCut();
        hillas = new Moments();
        stereo = new Stereo();
    }

    @After
    public void tearDown() throws Exception {
        s.close();
    }

    @Test
    public void testStereo() throws Exception {
        Data data = s.read();
        while (data != null){
            tailCut.process(data);
            hillas.process(data);
            stereo.process(data);

            data = s.read();
        }
    }
}
