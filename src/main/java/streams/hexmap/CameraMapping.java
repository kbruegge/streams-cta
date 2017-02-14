package streams.hexmap;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kbruegge on 2/13/17.
 */
public class CameraMapping {
    private static Logger log = LoggerFactory.getLogger(CameraMapping.class);

    private static final Type CAMERA_DEF = new TypeToken<Map<String, CameraGeometry>>() {}.getType();
    private static final Type ARRAY_DEF= new TypeToken<List<TelescopeDefinition>>() {}.getType();

    private static CameraMapping mapping;

    private final Map<String, CameraGeometry> cameras;
    private final ArrayList<TelescopeDefinition> telescopes;

    public synchronized static CameraMapping getInstance() {
        if (mapping ==  null){
            final URL cameraDefs = CameraGeometry.class.getResource("/hexmap/cta_camera_definitions.json");
            final URL arrayDef= CameraGeometry.class.getResource("/hexmap/cta_array_definition.json");

            try{
                mapping = new CameraMapping(arrayDef, cameraDefs);
            } catch (FileNotFoundException e){
                log.error("Could not load array or camera definitons from files. Do they exist?");
                throw new InstantiationError();
            }
        }
        return mapping;
    }

    private CameraMapping(URL arrayDef, URL cameraDefs) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        FileReader reader = new FileReader(cameraDefs.getFile());
        this.cameras = gson.fromJson(reader, CAMERA_DEF);

        reader = new FileReader(arrayDef.getFile());
        this.telescopes = gson.fromJson(reader, ARRAY_DEF);

    }

    public CameraGeometry cameraFromId(int telescopeId){
        String name = telescopes.get(telescopeId).cameraName;
        return cameras.get(name);
    }
}
