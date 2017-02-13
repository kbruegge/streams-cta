package streams.cta.io;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import streams.hexmap.CameraGeometry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;

import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Simply read the images from the json file to a map.
 * Created by kbruegge on 2/13/17.
 */
public class ImagesReader {

    private URL images = CameraGeometry.class.getResource("/images.json.gz");
    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static final Type IMAGE_DEF= new TypeToken<Map<Integer, double[]>>() {}.getType();


    @Test
    public void readArrayDefinition() throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new GZIPInputStream(images.openStream()), "UTF-8");
        JsonReader reader = new JsonReader(streamReader);
        reader.beginArray();
        while (reader.hasNext()) {
            Map<Integer, double[]> image = gson.fromJson(reader, IMAGE_DEF);
        }
    }

}
