package streams.cta.io;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import streams.hexmap.TelescopeDefinition;
import streams.hexmap.CameraGeometry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test whether the camera and array definitions can be read into the appropriate data structures
 * Created by kbruegge on 2/13/17.
 */
public class DefinitionsReader {

    private URL cameraDefs = CameraGeometry.class.getResource("/hexmap/cta_camera_definitions.json");
    private URL arrayDef= CameraGeometry.class.getResource("/hexmap/cta_array_definition.json");

    private static final Type CAMERA_DEF = new TypeToken<Map<String, CameraGeometry>>() {}.getType();
    private static final Type ARRAY_DEF= new TypeToken<List<TelescopeDefinition>>() {}.getType();

    @Test
    public void readCameraDefinitions() throws FileNotFoundException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        FileReader reader = new FileReader(cameraDefs.getFile());
        Map<String, CameraGeometry> cameras = gson.fromJson(reader, CAMERA_DEF);
    }

    @Test
    public void readArrayDefinition() throws FileNotFoundException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        FileReader reader = new FileReader(arrayDef.getFile());
        ArrayList<TelescopeDefinition> tels = gson.fromJson(reader, ARRAY_DEF);
    }

}
