package streams.cta.io;

import com.google.common.primitives.Ints;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

/**
 * Read images stored into json format as created by the 'convert_raw_data.py' sceript in this repo.
 * Created by kbruegge on 2/14/17.
 */
public class ImageStream extends AbstractStream {

    public ImageStream(SourceURL url) {
        super(url);
    }

    public ImageStream() {
    }

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static final Type IMAGE_DEF = new TypeToken<Map<Integer, double[]>>() {}.getType();
    private JsonReader reader;

    @Override
    public void init() throws Exception {
        super.init();
        InputStreamReader streamReader = new InputStreamReader(url.openStream(), "UTF-8");
        reader = new JsonReader(streamReader);
        reader.beginArray();
    }


    @Override
    public Data readNext() throws Exception {

        //check whether the end of the file has been reached
        JsonToken token = reader.peek();
        if (token == JsonToken.END_ARRAY) {
            return null;
        }

        Map<Integer, double[]> images = gson.fromJson(reader, IMAGE_DEF);

        Data data = DataFactory.create();
        images.forEach((telId, image) -> {
            data.put(String.format("telescope:%d:raw:photons", telId), image);
        });

        int[] triggeredTelescopeIds = Ints.toArray(images.keySet());
        data.put("triggered_telescopes:ids", triggeredTelescopeIds);

        return data;
    }

    @Override
    public void close() throws Exception {
        super.close();
        reader.close();
    }
}
