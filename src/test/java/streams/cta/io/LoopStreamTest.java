package streams.cta.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Map;

import stream.Data;
import stream.io.SourceURL;
import stream.io.Stream;

import static org.junit.Assert.*;

/**
 * Test looping over a substream (using ImageStream here).
 *
 * Created by alexey on 02.03.17.
 */
public class LoopStreamTest {

    private URL images = ImageStream.class.getResource("/images.json.gz");
    ImageStream imageStream;
    LoopStream loopStream;

    @Before
    public void setUp() throws Exception {
        imageStream = new ImageStream(new SourceURL(images));
        loopStream = new LoopStream();
        loopStream.addStream("imagestream", imageStream);
    }

    @After
    public void tearDown() throws Exception {
        imageStream.close();
    }

    @Test
    public void testInit() throws Exception {
        loopStream.init();

        Map<String, Stream> streams = loopStream.getStreams();
        assertTrue("Wrong number of sub-streams", streams.size() == 1);
    }

    @Test
    public void testReadNext() throws Exception {
        loopStream.init();

        Data first = loopStream.readNext();
        for (int i = 0; i < loopStream.events - 1; i++) {
            loopStream.readNext();
        }
        Data last = loopStream.readNext();

        for (int i = 0; i < (int) first.get("array:num_triggered_telescopes"); i++){
            int firstTel = ((int[])first.get("array:triggered_telescopes"))[i];
            int lastTel = ((int[])last.get("array:triggered_telescopes"))[i];
            assertTrue("Triggered telescope does not match when loop starts again.",
                    firstTel == lastTel);
        }
    }
}