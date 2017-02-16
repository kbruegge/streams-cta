package streams.cta.io;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.InputStreamReader;
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


    private class MC {
        double energy, alt, az, coreY, coreX;
    }
    private class Array {
        int[] triggeredTelescopes;
        int numTriggeredTelescopes;
    }
    private class Event{
        Map<Integer, double[]> images;
        MC mc;
        Array array;
        long eventId;
    }

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

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

        Event event = gson.fromJson(reader, Event.class);

        Data data = DataFactory.create();
        event.images.forEach((telId, image) -> {
            data.put(String.format("telescope:%d:raw:photons", telId), image);
        });

        data.put("array:triggered_telescopes", event.array.triggeredTelescopes);
        data.put("array:num_triggered_telescopes", event.array.numTriggeredTelescopes);

        data.put("mc:alt", event.mc.alt);
        data.put("mc:az", event.mc.az);
        data.put("mc:core_x", event.mc.coreX);
        data.put("mc:core_y", event.mc.coreY);
        data.put("mc:energy", event.mc.energy);

        data.put("event_id", event.eventId);

        return data;
    }

    @Override
    public void close() throws Exception {
        super.close();
        reader.close();
    }
}
