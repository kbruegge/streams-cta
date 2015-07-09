package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;

/**
 * Created by alexey on 30.06.15.
 */
public class PixelTiming {

    /**
     * is pixel timing data known?
     */
    boolean known;

    /**
     * Telescope ID
     */
    int telId;

    /**
     * Pixels in camera: list should be in this range.
     */
    int numPixels;

    /**
     * Number of different gains per pixel.
     */
    int numGains;

    /**
     * 0: not set; 1: individual pixels; 2: pixel ranges.
     */
    int listType;

    /**
     * The size of the pixels in this list.
     */
    int listSize;

    /**
     * The actual list of pixel numbers.
     */
    int[] pixelList;

    /**
     * Minimum base-to-peak raw amplitude difference applied in pixel selection.
     */
    int threshold;

    /**
     * Number of bins before peak being summed up.
     */
    int beforePeak;

    /**
     * Number of bins after peak being summed up.
     */
    int afterPeak;

    /**
     * How many different types of times can we store?
     */
    int numTypes;

    /**
     * Which types come in which order.
     */
    int[] timeType;

    /**
     * The width and startpos types apply above some fraction from base to peak.
     */
    float[] timeLevel;

    /**
     * Actually stored are the following timvals divided by granularity, as 16-bit integers. Set
     * this to e.g. 0.25 for a 0.25 time slice stepping.
     */
    float granularity;

    /**
     * Camera-wide (mean) peak position [time slices].
     */
    float peakGlobal;

    /**
     * Only the first 'pixels' elements are actually filled and stored. Others are undefined.
     */
    float[][] timval;

    /**
     * Amplitude sum around local peak, for pixels in list. Ped. subtr. Only present if
     * before&afterPeak>=0.
     */
    int[][] pulseSumLoc;

    /**
     * Amplitude sum around global peak; for all pixels. Ped. subtracted. Only present if
     * before&afterPeak>=0 and if list is of size>0 (otherwise no peak).
     */
    int[][] pulseSumGlob;

    //TODO constructor that does not need max values for array size
    public PixelTiming() {
        pixelList = new int[Constants.H_MAX_PIX * 2];
        timeType = new int[Constants.H_MAX_PIX_TIMES];
        timeLevel = new float[Constants.H_MAX_PIX_TIMES];
        timval = new float[Constants.H_MAX_PIX][Constants.H_MAX_PIX_TIMES];
        pulseSumLoc = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        pulseSumGlob = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
    }

    public boolean readPixTime(EventIOBuffer buffer) {
        //TODO implement
        return false;
    }
}
