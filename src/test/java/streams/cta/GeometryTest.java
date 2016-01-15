package streams.cta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import streams.hexmap.FactHexPixelMapping;
import streams.hexmap.LSTHexPixelMapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * Created by kai on 15.01.16.
 */
public class GeometryTest {

    @Test
    public void testCubeCoordinates() throws IOException {
        URL u = GeometryTest.class.getResource("/test_geometry_fact.json");
        Gson gson = new GsonBuilder().create();
        File f = new File(u.getFile());
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        FactHexPixelMapping mapping = FactHexPixelMapping.getInstance();
        bw.write(gson.toJson(mapping.getAllPixel()));
        bw.close();
    }

    @Test
    public void testCubeCoordinatesLST() throws IOException {
        URL u = GeometryTest.class.getResource("/test_geometry_lst.json");
        Gson gson = new GsonBuilder().create();
        File f = new File(u.getFile());
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        LSTHexPixelMapping mapping = LSTHexPixelMapping.getInstance();
        bw.write(gson.toJson(mapping.getAllPixel()));
        bw.close();
    }
}
