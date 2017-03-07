package streams.hexmap;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a singleton providing access to geometrical camera descriptions (aka CameraGeometry
 * objects) by the specified telescope/camera id. These ids and camera descroptions are defined in
 * the json files stored in the resources folder : 'cta_array_definition.json' and
 * 'cta_camera_definition.json'.
 *
 * Created by kbruegge on 2/13/17.
 */
public class CameraMapping {
    private static Logger log = LoggerFactory.getLogger(CameraMapping.class);

    private static final Type CAMERA_DEF = new TypeToken<HashMap<String, CameraGeometry>>() {}.getType();
    private static final Type ARRAY_DEF = new TypeToken<ArrayList<TelescopeDefinition>>() {}.getType();

    /**
     * Singleton instance containing information about cameras' geometry and telescope definition
     */
    private static CameraMapping mapping;

    /**
     * Map with camera geometries (value) for different cameras (key).
     */
    private final Map<String, CameraGeometry> cameras;

    /**
     * List of various defined telescopes.
     */
    private final ArrayList<TelescopeDefinition> telescopes;

    /**
     * Retrieve the singleton instance of the camera mapping which contains geometry data for the
     * cameras and definition of the telescopes
     */
    public synchronized static CameraMapping getInstance() {
        if (mapping == null) {
            try {
                mapping = new CameraMapping();
            } catch (FileNotFoundException e) {
                log.error("Could not load array or camera definitions from files. Do they exist?");
                throw new InstantiationError();
            }
        }
        return mapping;
    }

    private CameraMapping() throws FileNotFoundException {
        Gson gson = new GsonBuilder().
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        Class cl = CameraGeometry.class;

        // initialize geometry for the cameras
        final InputStream cameraDefs = cl.getResourceAsStream("/hexmap/cta_camera_definitions.json");
        InputStreamReader reader = new InputStreamReader(cameraDefs);
        this.cameras = gson.fromJson(reader, CAMERA_DEF);

        // initialize definition for the telescopes
        final InputStream arrayDef = cl.getResourceAsStream("/hexmap/cta_array_definition.json");
        reader = new InputStreamReader(arrayDef);
        this.telescopes = gson.fromJson(reader, ARRAY_DEF);
    }

    /**
     * Get the camera geometry for the given id.
     *
     * @param telescopeId the id of the telescope to get
     * @return the camera geometry for the telescope
     */
    public CameraGeometry cameraFromId(int telescopeId) {
        //Watch out for the index here. Telescope ids start at 1.
        //The mapping from telescope index to array index hence needs a -1
        String name = telescopes.get(telescopeId - 1).cameraName;
        return cameras.get(name);
    }

    /**
     * Get the Telescope definition for the given id.
     *
     * @param telescopeId the id of the telescope to get
     * @return the TelescopeDefinition object describing this telescope
     */
    public TelescopeDefinition telescopeFromId(int telescopeId){
        //Telescopes ids start with one. Lists are zero based.
        //Let the bugs flow right out of this!
        return telescopes.get(telescopeId - 1);
    }
}
