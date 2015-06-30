package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.HTime;

/**
 * Event raw and image data from one telescope. Created by alexey on 30.06.15.
 */
public class TelEvent {
    int known;

    /**
     * The telescope ID number (1 ... n)
     */
    int telId;

    /**
     * The counter for local triggers.
     */
    int locCount;

    /**
     * The counter for system triggers.
     */
    int globCount;

    /**
     * Camera CPU system time of event.
     */
    HTime cpuTime;

    /**
     * GPS time of event, if any.
     */
    HTime gpsTime;

    /**
     * 1=internal (event data) or 2=external (calib data).
     */
    int trgSource;

    /**
     * Number of trigger groups (sectors) listed.
     */
    int numListTrgsect;

    /**
     * List of triggered groups (sectors).
     */
    int[] listTrgsect;

    /**
     * Are the trigger times known? (0/1)
     */
    int knownTimeTrgsect;

    /**
     * Times when trigger groups (as in list) fired.
     */
    double[] timeTrgsect;

//   char type_trgsect[H_MAX_SECTORS]; ///< 0: majority, 1: analog sum, 2: digital sum.

    /**
     * Sum mode (0) or sample mode (1 ... 255, normally: 1).
     */
    int readoutMode;

    /**
     * how many 'img' sets are available.
     */
    int numImageSets;

    /**
     * how many 'img' sets were allocated.
     */
    int maxImageSets;

    /**
     * Pointer to raw data, if any.
     */
    AdcData raw;

    /**
     * Optional pixel (pulse shape) timing.
     */
    PixelTiming pixtm;

    /**
     * Pointer to second moments, if any.
     */
    ImgData img;

    /**
     * Pointer to calibrated pixel intensities, if available.
     */
    PixelCalibrated pixcal;

//    int numPhysAddr;      ///< (not used)
//    int physAddr[4*H_MAX_DRAWERS];///< (not used)

    /**
     * List of triggered pixels.
     */
    PixelList triggerPixels;

    /**
     * Pixels included in (first) image.
     */
    PixelList imagePixels;

    public TelEvent() {
        listTrgsect = new int[Constants.H_MAX_SECTORS];
        timeTrgsect = new double[Constants.H_MAX_SECTORS];
        raw = new AdcData();
        pixtm = new PixelTiming();
        img = new ImgData();
        pixcal = new PixelCalibrated();
        triggerPixels = new PixelList();
        imagePixels = new PixelList();
    }
}