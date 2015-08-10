package streams.hexmap;

/**
 * This describes the stuff a pixel to be drawn on the getColorFromValue must have. This should be subclassed if you need
 * more information in each pixel
 * Created by kaibrugge on 23.04.14.
 */
public class CameraPixel {

    final public int id;
    final public int offsetCoordinateX;
    final public int offsetCoordinateY;
    final public double xPositionInMM;
    final public double yPositionInMM;

    public CameraPixel(int id, int offsetCoordinateX, int offsetCoordinateY, double xPositionInMM, double yPositionInMM) {
        this.id = id;
        this.offsetCoordinateX = offsetCoordinateX;
        this.offsetCoordinateY = offsetCoordinateY;
        this.xPositionInMM = xPositionInMM;
        this.yPositionInMM = yPositionInMM;
    }


    @Override
    public String toString(){
        return "Pixel " + this.id + " at position " + xPositionInMM + ", " + yPositionInMM;
    }


}
