package streams.cta.io;

import org.junit.Test;
import stream.Data;
import stream.Keys;
import stream.io.SourceURL;

import java.net.URL;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Simply read the images from the json file to a map.
 * Created by kbruegge on 2/13/17.
 */
public class ImagesReader {

    private URL images = ImageStream.class.getResource("/images.json.gz");

    @Test
    public void testStream() throws Exception {
        ImageStream s = new ImageStream(new SourceURL(images));

        s.init();

        Data data = s.read();
        while (data != null){
            assertThat(Keys.select(data, "telescope:*").isEmpty(), is(false));

            int[] ids = (int[]) data.get("array:triggered_telescopes");
            assertThat(ids.length, is(not(0)));

            double energy = (double) data.get("mc:energy");
            assertTrue(energy > 0);

            double x = (double) data.get("mc:core_x");
            double y= (double) data.get("mc:core_y");

            assertTrue(
                    "Impact parameter should be less than 100 000 meters. I guess",
                    sqrt(pow(x, 2) + pow(y, 2)) < 1000_000
            );

            data = s.read();
        }

        s.close();
    }
}
