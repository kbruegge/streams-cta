package streams.cta.io;

import com.google.common.io.Files;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import javafx.scene.Camera;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.hexmap.CameraGeometry;
import streams.hexmap.CameraMapping;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * Read images stored into json format as created by the 'convert_raw_data.py' script in this repo.
 * Created by kbruegge on 2/14/17.
 */
public class ImageStream extends AbstractStream {

    private CameraMapping mapping;

    public ImageStream(SourceURL url) {
        super(url);
    }
    public ImageStream() {
    }




    /**
     * One CTA event contains MC information, Array information and of course the images
     * for each camera.
     * The classes below mirror the structure of the JSON file which contains the CTA events.
     * By using this intermediate class structure we can simplify the reading of the json to one
     * single line. Because GSON is pretty nice.
     */
    private class Event{
        Map<Integer, double[]> images;
        MC mc;
        Array array;
        long eventId;
    }
    /**
     * Calibrated CTA Events. First up is the Monte-Carlo data.
     */
    private class MC {
        double energy, alt, az, coreY, coreX;
    }
    /**
     * Information about the event which is not specific to one single camera but to
     * the whole array at once. At some point this shoudl include a Timestamp I suppose.
     * The CTA monte-carlo does not have unique ids or timestamps from what I can see.
     */
    private class Array {
        int[] triggeredTelescopes;
        int numTriggeredTelescopes;
    }

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private JsonReader reader;

    @Override
    public void init() throws Exception {
        super.init();
        InputStreamReader streamReader = new InputStreamReader(url.openStream(), "UTF-8");
        reader = new JsonReader(streamReader);
        reader.beginArray();
        mapping = CameraMapping.getInstance();
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
            CameraGeometry.TelescopeType type = mapping.telescopeFromId(telId).telescopeType;
            data.put(String.format("telescope:%d:type:id", telId), type.ordinal());
            data.put(String.format("telescope:%d:type:name", telId), type.toString());
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

        //Add the filename of the file we read from. This will be used to map
        //event lists to MC production information.
        String filename = Files.getNameWithoutExtension(url.getFile()).replace(".json", "");
        data.put("source_file", filename);

        return data;
    }

    @Override
    public void close() throws Exception {
        super.close();
        reader.close();
    }
}
