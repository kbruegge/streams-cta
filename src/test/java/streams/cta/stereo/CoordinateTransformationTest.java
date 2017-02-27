package streams.cta.stereo;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test the java implementation of Tino Michael's FitGammaHillas routine from ctapipe.
 *
 * Created by kbruegge on 2/27/17.
 */
public class CoordinateTransformationTest {

    @Test
    public void testRotationAroundDiagonal(){
        double[] v = new double[]{1, 0, 0};
        double[] axis = new double[]{1, 1, 1};
        double[] rotatedVector = Stereo.rotateAroundAxis(v, axis, 0.1);

        //result from the python implementation in ctapipe. (which is horrible btw)
        double[] expectedResults = new double[] {0.99666944, -0.05597357,  0.05930413};
        assertArrayEquals(
                "Rotation around axis not consistend with python results",
                 expectedResults,
                 rotatedVector,
                0.0000001
        );
    }


    @Test
    public void testRotationAroundParallelAxis(){
        double[] vector = new double[]{1, 0, 0};
        double[] axis = new double[]{1, 0, 0};
        double[] rotatedVector = Stereo.rotateAroundAxis(vector, axis, 0.1);

        // i think this should be the correct asnwer. were rotating a vector around itself basically
        double[] expectedResults = new double[] {1, 0, 0};
        assertArrayEquals(
                "Rotation around parallel axis should not change anything",
                expectedResults,
                rotatedVector,
                0.0000001
        );
    }


    @Test
    public void testRotationWithNegativeComponent(){
        double[] vector = new double[]{-1, 0, 0};
        double[] axis = new double[]{1, 0, 0};
        double[] rotatedVector = Stereo.rotateAroundAxis(vector, axis, 23);

        // i think this should be the correct asnwer. were rotating a vector around itself basically
        double[] expectedResults = new double[] {-1, 0, 0};
        assertArrayEquals(
                "Rotation around parallel axis should not change anything",
                expectedResults,
                rotatedVector,
                0.0000001
        );
    }

    @Test
    public void testFlipVector(){
        //create a vector pointing in negative y direction
        double[] vector = new double[]{0, -1, 0};
        double[] axis = new double[]{1, 0, 0};
        //rotate around perpendicular axis by 180
        double[] rotatedVector = Stereo.rotateAroundAxis(vector, axis, Math.PI);

        // it should still point along the y axis but in the opposite direction
        double[] expectedResults = new double[] {0, 1, 0};
        assertArrayEquals(
                "Rotation by 180 degree should not change the values but only th sign",
                expectedResults,
                rotatedVector,
                0.0000000001
        );
    }

    @Test
    public void testPixelDirectionOfCameraCenter(){
        double phi = Math.toRadians(45);
        double theta = Math.toRadians(45);
        double[] direction = Stereo.pixelDirection(0, 0, phi, theta, 4.8, 0);

        double[] expectedResults = new double[] {0.5, 0.5, 0.70710678};
        assertArrayEquals
                (
                "pixel direction should match python implementation",
                 expectedResults,
                 direction,
                0.000001
                );

    }


    @Test
    public void testPixelDirection(){
        double phi = Math.toRadians(45);
        double theta = Math.toRadians(45);
        double[] direction = Stereo.pixelDirection(0.05, 0.05, phi, theta, 4.8, 0);

        double[] expectedResults = new double[] { 0.51251932, 0.49778846, 0.69966463};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }



    @Test
    public void testPixelDirectionThirdExample(){
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(20);
        double[] direction = Stereo.pixelDirection(0.05, 0.05, phi, theta, 4.8, 0);

        double[] expectedResults = new double[] {  0.35177114,-0.01041629, 0.93602808};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }

    @Test
    public void testPixelDirectionFourthExample(){
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(20);
        double[] direction = Stereo.pixelDirection(1, 0, phi, theta, 28, 0);

        double[] expectedResults = new double[]  {0.37535536, 0.        , 0.92688098};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }


    @Test
    public void testPixelDirectionFifthExample(){
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(20);
        double[] direction = Stereo.pixelDirection(1, 1, phi, theta, 4.8, 0);

        double[] expectedResults = new double[]  {0.52023185,-0.20533231, 0.82897375};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }

    @Test
    public void testPixelDirectionSixthExample(){
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(20);
        double[] direction = Stereo.pixelDirection(1, 0, phi, theta, 4.8, 0);

        double[] expectedResults = new double[]  {0.52898085, 0.        , 0.84863376};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }

    @Test
    public void testPixelDirectionSecondExample(){
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(20);
        double[] direction = Stereo.pixelDirection(0.14642135623730954, 0.44142135623730949, phi, theta, 28, 0);

        double[] expectedResults = new double[] {  0.34688671,-0.01576432, 0.93777455};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );

    }

    @Test
    public void testHorizonDirection(){
        //point to horizon along x axis
        double phi = Math.toRadians(0);
        double theta = Math.toRadians(90);
        //use the center of the camera
        double[] direction = Stereo.pixelDirection(0.0, 0.0, phi, theta, 4.8, 0);

        // we should point into some direction along one axis. since the output is normalized this
        // component should be 1 I suppose.
        double[] expectedResults = new double[] { 1, 0, 0};
        assertArrayEquals
                (
                        "pixel direction should match python implementation",
                        expectedResults,
                        direction,
                        0.000001
                );
    }

    @Test
    public void testPolarToCartesian(){
        double[] direction = Stereo.cartesianFromPolar(0, 0);

        //in iso convetion this should point along the z axis
        double[] expectedResults = new double[] { 0, 0, 1};
        assertArrayEquals
                (
                        "vector should point along z axis",
                        expectedResults,
                        direction,
                        1.0E-14
                );
    }


    @Test
    public void testPolarToCartesianAlongHorizon(){

        double[] direction = Stereo.cartesianFromPolar(0, Math.toRadians(90));

        //in iso convetion this should point along the x axis
        double[] expectedResults = new double[] { 1, 0, 0};
        assertArrayEquals
                (
                        "vector should point along z axis",
                        expectedResults,
                        direction,
                        1.0E-14
                );
    }
}
