package streams.hexmap;

/**
 * Created by kbruegge on 2/13/17.
 */
public class CameraGeometry {

    int numberOfPixel;

    double[] pixelXPositions;
    double[] pixelYPositions;

    double pixelRotation;

    PixelType pixelType;

    int[] pixelIds;

    double[] pixelArea;

    double cameraRotation;

    int[][] neighbours;

    TelescopeType telescopeType;

    String name;


    /**
     * The pixel geometry.
     */
    public enum PixelType {
        RECTANGULAR("rectangular"),
        HEXAGONAL("hexagonal");

        String geometry;

        PixelType(String geometry) {
            this.geometry = geometry;
        }
    }


    /**
     * The telescope type.
     */
    public enum TelescopeType {
        SST("SST"),
        MST("MST"),
        LST("LST");

        String type;

        TelescopeType(String type) {
            this.type = type;
        }
    }

}
