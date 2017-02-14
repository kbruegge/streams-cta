package streams.hexmap;

/**
 * Created by kbruegge on 2/13/17.
 */
public class CameraGeometry {

    public int numberOfPixel;

    public double[] pixelXPositions;
    public double[] pixelYPositions;

    double pixelRotation;

    PixelType pixelType;

    public int[] pixelIds;

    double[] pixelArea;

    double cameraRotation;

    public int[][] neighbours;

    TelescopeType telescopeType;

    public String name;


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
