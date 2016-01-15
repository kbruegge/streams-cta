package streams.hexmap;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by kaibrugge on 23.04.14.
 */
public class FactCameraPixel extends CameraPixel implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;

    final public int chid;
    final public int board;
    final public int softid;
    final public int crate;
    final public int patch;
    final public int hardid;
    final public int drs_chip;


    public FactCameraPixel(int chid, int softid, int hardid, int q, int r, double posX, double posY) {
        super(chid , q, r, posX, posY);
        this.softid = softid;
        this.hardid = hardid;
        this.crate = hardid / 1000;
        this.board = (hardid / 100) % 10;
        this.patch = (hardid / 10) % 10;
        this.chid  = (hardid % 10) + 9 * patch + 36 * board + 360 * crate;
        this.drs_chip = chid / 9;
    }



    /**
     * This function returns the data contained in this pixel from the big data array containing the data for all pixels
     * @param data the array containing the data for all pixels
     * @param roi the region of interest in the data. Usually 300 slices or 1024
     * @return the data for this pixel
     */
    public double[] getPixelData(double[] data, int roi){
        double[] pixelData = Arrays.copyOfRange(data, id*roi, id*roi + roi);
        return pixelData;
    }


}
