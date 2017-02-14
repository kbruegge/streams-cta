package streams.cta.denoising;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;
import streams.cta.cleaning.TwoLevelTimeNeighbor;
import streams.hexmap.CameraGeometry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by kbruegge on 2/13/17.
 */
public class CleaningTest {


    private URL images = CameraGeometry.class.getResource("/images.json.gz");
    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static final Type IMAGE_DEF= new TypeToken<Map<Integer, double[]>>() {}.getType();


    public Map<Integer, double[]>  readImages() throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new GZIPInputStream(images.openStream()), "UTF-8");
        JsonReader reader = new JsonReader(streamReader);
        reader.beginArray();

        return gson.fromJson(reader, IMAGE_DEF);
    }

    @Test
    public void testCleaning() throws IOException {

        Map<Integer, double[]> images = readImages();
        Data item = DataFactory.create();
        item.put("images", (Serializable) images);

        TwoLevelTimeNeighbor cl = new TwoLevelTimeNeighbor();
        cl.levels = new Double[]{2700.0, 2100.0, 2250.0};
        cl.process(item);
        System.out.println(item);
    }
}
