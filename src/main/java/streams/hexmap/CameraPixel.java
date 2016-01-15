package streams.hexmap;

/**
 * This describes the stuff a pixel to be drawn on the getColorFromValue must have. This should be subclassed if you need
 * more information in each pixel
 * Created by kaibrugge on 23.04.14.
 */
public class CameraPixel {

    final public int id;
    final public int axialQ;
    final public int axialR;
    final public int cubeX;
    final public int cubeY;
    final public int cubeZ;
    final public double xPositionInMM;
    final public double yPositionInMM;

    public CameraPixel(int id, int q, int r, double xPositionInMM, double yPositionInMM) {
        this.id = id;
        this.axialQ= q;
        this.axialR= r;
        this.xPositionInMM = xPositionInMM;
        this.yPositionInMM = yPositionInMM;
        this.cubeX = q;
        this.cubeZ = r;
        this.cubeY = -cubeX - cubeZ;
    }


    @Override
    public String toString(){
        return "Pixel " + this.id + " at position " + xPositionInMM + ", " + yPositionInMM;
    }


}
