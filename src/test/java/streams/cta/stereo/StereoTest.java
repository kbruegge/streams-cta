package streams.cta.stereo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.flow.ForEach;
import stream.io.SourceURL;
import streams.MergeByTelescope;
import streams.SplitByTelescope;
import streams.cta.cleaning.TailCut;
import streams.cta.features.Moments;
import streams.cta.io.ImageStream;
import streams.hexmap.CameraGeometry;

/**
 * Test the stereo features processor in a complete pipeline.
 *
 * Created by kbruegge on 2/27/17.
 */
public class StereoTest {


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

        merge = new MergeByTelescope();
        merge.setKey(splitKey);
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }

    @Test
    public void testStereo() throws Exception {
        Data data = stream.read();

        TailCut tailCut = new TailCut();
        Moments hillas = new Moments();
        Stereo stereo = new Stereo();

        forEach = new ForEach();
        forEach.setKey(splitKey);
        forEach.add(tailCut);
        forEach.add(hillas);


        while (data != null){
            Data splitData = split.process(data);
            Data foreachData = forEach.process(splitData);
            data = merge.process(foreachData);

            stereo.process(data);

            data = stream.read();
        }
    }
}
