package streams.cta.io;

import streams.cta.Constants;

/**
 * EventIOData can be compared to AllHessData data type in hessioxxx Created by alexey on 24.06.15.
 */
public class EventIOData {
    MCShower mcShower;

    /**
     * needed to read the event
     */
    FullEvent event;
//    RunHeader run_header;
//    MCEvent mc_event;
//    MCRunHeader mc_run_header;
//    CameraSettings camera_set[H_MAX_TEL];

    // TODO should be used later
//    CameraOrganisation camera_org[H_MAX_TEL];
//    PixelSetting pixel_set[H_MAX_TEL];
//    PixelDisabled pixel_disabled[H_MAX_TEL];
//    CameraSoftSet cam_soft_set[H_MAX_TEL];
//    TrackingSetup tracking_set[H_MAX_TEL];
//    PointingCorrection point_cor[H_MAX_TEL];
//    // MCpeSum mc_pesum;
//    TelMoniData tel_moni[H_MAX_TEL];
//    LasCalData tel_lascal[H_MAX_TEL];
//    RunStat run_stat;
//    MCRunStat mc_run_stat;
}

/**
 * All data for one event
 */
class FullEvent {

    /**
     * Number of telescopes in run.
     */
    int numTel;

    /**
     * Central trigger data and data pattern.
     */
    CentralEvent central;

    /**
     * Raw and/or image data.
     */
    TelEvent[] teldata;

    /**
     * Interpolated tracking data.
     */
    TrackEvent[] trackdata;

    /**
     * Reconstructed shower parameters.
     */
    ShowerParameters shower;

    /**
     * Number of telescopes for which we actually have data.
     */
    int numTeldata;

    /**
     * List of IDs of telescopes with data.
     */
    int[] teldataList;

    public FullEvent() {
        numTel = 0;
        numTeldata = 0;
        teldata = new TelEvent[Constants.H_MAX_TEL];
        trackdata = new TrackEvent[Constants.H_MAX_TEL];
        shower = new ShowerParameters();
        teldataList = new int[Constants.H_MAX_TEL];
        central = new CentralEvent();
    }
}


/**
 * Central trigger event data
 */
class CentralEvent {

    /**
     * Global event count.
     */
    int globCount;

    /**
     * CPU time at central trigger station.
     */
    HTime cpuTime;

    /**
     * GPS time at central trigger station.
     */
    HTime gpsTime;

    /**
     * Bit pattern of telescopes having sent a trigger signal to the central station. (Historical;
     * only useful for small no. of telescopes.)
     */
    int teltrgPattern;

    /**
     * Bit pattern of telescopes having sent event data that could be merged. (Historical; only
     * useful for small no. of telescopes.)
     */
    int teldataPattern;

    /**
     * How many telescopes triggered.
     */
    int numTeltrg;

    /**
     * List of IDs of triggered telescopes.
     */
    int[] teltrgList;

    /**
     * Relative time of trigger signal after correction for nominal delay [ns].
     */
    float[] teltrgTime;

    /**
     * Bit mask which type of trigger fired.
     */
    int[] teltrgTypeMask;

    /**
     * Time of trigger separate for each type.
     */
    float[][] teltrgTimeByType;

    /**
     * Number of telescopes expected to have data.
     */
    int numTeldata;

    /**
     * List of IDs of telescopes with data.
     */
    int[] teldataList;

    public CentralEvent() {
        teltrgList = new int[Constants.H_MAX_TEL];
        teltrgTime = new float[Constants.H_MAX_TEL];
        teltrgTypeMask = new int[Constants.H_MAX_TEL];
        teltrgTimeByType = new float[Constants.H_MAX_TEL][3];
        teldataList = new int[Constants.H_MAX_TEL];
    }
}

/**
 * Breakdown of time into seconds since 1970.0 and nanoseconds.
 */
//TODO any better java structure for this purpose?!
class HTime {
    long seconds;
    long nanoseconds;
}

/**
 * Event raw and image data from one telescope.
 */
class TelEvent {
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
