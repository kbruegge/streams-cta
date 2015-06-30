package streams.cta.io.Event;

import streams.cta.Constants;

/**
 * Created by alexey on 30.06.15.
 */
public class PixelCalibrated {

    /**
     * is calibrated pixel data known?
     */
    int known;

    /**
     * Telescope ID
     */
    int telId;

    /**
     * Pixels in camera: list should be in this range.
     */
    int numPixels;

    /**
     * -2 (timing local peak), -1 (timing global peak), >=0 (integration scheme, if known)
     */
    int intMethod;

    /**
     * Was list of significant pixels filled in? 1: use list, 2: all pixels significant
     */
    int listKnown;

    /**
     * Size of the list of available pixels (with list mode).
     */
    int listSize;

    /**
     * List of available pixels (with list mode).
     */
    int[] pixelList;

    /**
     * Was amplitude large enough to record it?
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    short[] significant;

    /**
     * Calibrated & flat-fielded pixel intensity [p.e.]
     */
    float[] pixelPe;

    public PixelCalibrated() {
        pixelList = new int[Constants.H_MAX_PIX];
        significant = new short[Constants.H_MAX_PIX];
        pixelPe = new float[Constants.H_MAX_PIX];
    }
}