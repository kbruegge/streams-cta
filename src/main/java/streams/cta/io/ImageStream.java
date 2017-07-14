package streams.cta.io;

import com.google.common.io.Files;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import javafx.scene.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.hexmap.CameraGeometry;
import streams.hexmap.CameraMapping;

import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read images stored into json format as created by the 'convert_raw_data.py' script in this repo.
 * Created by kbruegge on 2/14/17.
 */
public class ImageStream extends AbstractStream {
    static Logger log = LoggerFactory.getLogger(ImageStream.class);
    private CameraMapping mapping;
    private Integer run_id = null;

    public ImageStream(SourceURL url) {
        super(url);
    }
    public ImageStream() {
    }


    private static final Pattern RUN_NUMBER= Pattern.compile(".*_run(\\d*)_.*(json).*");

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
        String timestamp;
    }
    /**
     * The Monte-Carlo information in the data contains the true values for direction and energy.
     * Saving the type of the primary particle might also be useful.
     */
    private class MC {
        double energy, alt, az, coreY, coreX;
        String type;
    }
    /**
     * Information about the event which is not specific to one single camera but to
     * the whole array at once. At some point this should include a Timestamp I suppose.
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
        String filename = Paths.get(url.getPath()).getFileName().toString();
        Matcher matcher = RUN_NUMBER.matcher(filename);
        if (matcher.find()){
            run_id = Integer.parseInt(matcher.group(1));
        } else {
            log.warn("Could no extract run_id from filename {}", filename);
        }
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
        data.put("mc:type", event.mc.type);

        data.put("event_id", event.eventId);
        data.put("timestamp", event.timestamp);

        if (run_id != null){
            data.put("run_id", run_id);
            //assuming there are less than a 10 million runs in the montecarlo production, this generates a unique id
            //for each array-wide event
            data.put("unique_event_id", ((run_id) << 7) + event.eventId);
        }



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
