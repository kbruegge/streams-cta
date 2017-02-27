package streams.cta.stereo;

import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * Test the planes class from the stereo reconstruction.
 * Created by kbruegge on 2/27/17.
 */
public class PlanesTest {

    private Data createFakeItem(int telescopeId, double cogX, double cogY, double psi){

        Map<String, Serializable> m = new HashMap<>();

        int id = telescopeId;
        m.put("telescope:" + id + ":shower:length", 0.2);
        m.put("telescope:" + id + ":shower:width", 0.1);
        m.put("telescope:" + id + ":shower:cog:x", cogX);
        m.put("telescope:" + id + ":shower:cog:y", cogY);
        m.put("telescope:" + id + ":shower:psi", psi);
        m.put("telescope:" + id + ":shower:size", 1000.0);
        return DataFactory.create(m);
    }

    @Test
    public void testCreation(){
        double phi = 0.0;
        double theta = 20.0;

        Data tel1 = createFakeItem(1, 0.005, 0.3, Math.toRadians(45));

        Stereo.Plane plane = new Stereo.Plane(1,phi, theta, tel1);
        assertThat(plane.telescopeId, is(1));

        //make sure these are different
        Data tel2 = createFakeItem(2, 0.01, 0.3, Math.toRadians(45));
        Stereo.Plane otherPlane = new Stereo.Plane(2, phi, theta,  tel2);
        assertThat(plane, is(not(otherPlane)));
    }


    @Test
    public void compareValues(){
        double phi = 0.0;
        double theta = Math.toRadians(20.0);


        Data data = createFakeItem(1, 0.005, 0.3, Math.toRadians(45));
        Stereo.Plane plane = new Stereo.Plane(1,phi, theta, data);
        assertThat(plane.telescopeId, is(1));
        assertThat(plane.weight, is(2000.0));

        //results from the python implementation for a telescope with focal length 28m.
        double[] expected = new double[]{0.66702968,  0.70704757, -0.23485133};
        assertArrayEquals(expected, plane.normalVector, 0.000001);
    }


    @Test
    public void compareValuesSecondExample(){
        double phi = 0.0;
        double theta = Math.toRadians(20.0);

        Data data = createFakeItem(2, 0.01, 0.3, Math.toRadians(40));
        Stereo.Plane plane = new Stereo.Plane(2,phi, theta, data);

        //results from the python implementation for a telescope with focal length 28m.
        double[] expected = new double[]{0.60677317,  0.76598337, -0.21235769};
        assertArrayEquals(expected, plane.normalVector, 0.000001);
    }


    @Test
    public void testDirectionEstimator(){
        double phi = 0.0;
        double theta = Math.toRadians(20.0);
        Data tel1 = createFakeItem(1, 0.005, 0.3, Math.toRadians(45));
        Data tel2 = createFakeItem(2, 0.01, 0.3, Math.toRadians(40));

        Stereo.Plane plane1 = new Stereo.Plane(1,phi, theta, tel1);
        Stereo.Plane plane2 = new Stereo.Plane(2,phi, theta, tel2);

        Stereo stereo = new Stereo();

        double[] direction = stereo.estimateDirection(Lists.newArrayList(plane1, plane2));


        //results from the python implementation
        double[] expectedDirection = new double[]{0.34129638,-0.00978281, 0.93990483};

        assertArrayEquals(expectedDirection, direction, 0.00001);
    }

    @Test
    public void testCorePositionEstimator(){
        double phi = 0.0;
        double theta = Math.toRadians(20.0);
        Data tel1 = createFakeItem(1, 0.005, 0.3, Math.toRadians(45));
        Data tel2 = createFakeItem(2, 0.01, 0.3, Math.toRadians(40));

        Stereo.Plane plane1 = new Stereo.Plane(1,phi, theta, tel1);
        Stereo.Plane plane2 = new Stereo.Plane(2,phi, theta, tel2);

        Stereo stereo = new Stereo();

        double[] position = stereo.estimateCorePosition(Lists.newArrayList(plane1, plane2));


        //results from the python implementation
        double[] expectedPosition = new double[]{-1012.2812074 ,  861.41655442};

        assertArrayEquals(expectedPosition, position, 0.00001);
    }
}
