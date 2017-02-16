package streams.cta.io;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import stream.Data;
import stream.Keys;
import stream.io.SourceURL;
import streams.hexmap.CameraGeometry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


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
            data = s.read();
        }

        s.close();
    }
}
