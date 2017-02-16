package streams.cta.features;

import org.junit.Test;
import streams.hexmap.CameraGeometry;
import streams.hexmap.CameraMapping;
import streams.hexmap.Shower;

import static org.junit.Assert.assertEquals;

/**
 * Test some aspects of the Cog calculations.
 * Created by kbruegge on 2/15/17.
 */
public class COGTest {

    @Test
    public void cogSanity() throws Exception {
        COG processor = new COG();

        int telescopeId = 1;
        CameraGeometry geometry = CameraMapping.getInstance().cameraFromId(telescopeId);

        //create empty shower in some camera
        Shower s = new Shower(telescopeId);

        COG.CoGPoint point = processor.calculateCenterOfGravity(s);

        assertEquals(point.cogX, Double.NaN, 0.0);
        assertEquals(point.cogY, Double.NaN, 0.0);

        int pixelId = 0;

        s.addPixel(pixelId, 1);

        point = processor.calculateCenterOfGravity(s);

        assertEquals(
                "Center of gravity should match pixel x coordinate exactly",
                point.cogX,
                geometry.pixelXPositions[pixelId],
                0.0);
        assertEquals(
                "Center of gravity should match pixel y coordinate exactly",
                point.cogY,
                geometry.pixelYPositions[pixelId],
                0.0);

    }
}
