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


/**
 * ADC data (either sampled or sum mode)
 */
class AdcData {

    /**
     * Must be set to 1 if and only if raw data is available.
     */
    int known;

    /**
     * Must match the expected telescope ID when reading.
     */
    int telId;

    /**
     * The number of pixels in the camera (as in configuration)
     */
    int numPixels;

    /**
     * The number of different gains per pixel (2 for HESS).
     */
    int numGains;

    /**
     * The number of samples (time slices) recorded.
     */
    int numSamples;

    /**
     * The desired or used zero suppression mode.
     */
    int zeroSupMode;

    /**
     * The desired or used data reduction mode.
     */
    int dataRedMode;

    /**
     * The offset to be used in shrinking high-gain data.
     */
    int offsetHg8;

    /**
     * The scale factor (denominator) in shrinking h-g data.
     */
    int scaleHg8;

    /**
     * Threshold (in high gain) for recording low-gain data.
     */
    int threshold;

    /**
     * Was list of significant pixels filled in?
     */
    int listKnown;

    /**
     * Size of the list of available pixels (with list mode).
     */
    int listSize;

    /**
     * List of available pixels (with list mode).
     */
    int[] adcList;

    /**
     * Was amplitude large enough to record it? Bit 0: sum, 1: samples. uint8_t
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    short[] significant;

    /**
     * Was individual channel recorded? Bit 0: sum, 1: samples, 2: ADC was in saturation. uint8_t
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    short[][] adcKnown;

    /**
     * Sum of ADC values. uint32_t
     */
    // TODO use LONG or maybe INT but using 'intValue & 0xffffffff' for
    // arithmetic operations? http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    long[][] adcSum;

    /**
     * Pulses sampled. uint16_t
     */
    // TODO use INT or maybe SHORT but using 'intValue & 0xffff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    int[][][] adcSample;

    public AdcData() {
        adcList = new int[Constants.H_MAX_PIX];
        significant = new short[Constants.H_MAX_PIX];
        adcKnown = new short[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        adcSum = new long[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        adcSample = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX][Constants.H_MAX_SLICES];
    }
}

class PixelTiming {

    /**
     * is pixel timing data known?
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

    public PixelTiming() {
        pixelList = new int[Constants.H_MAX_PIX * 2];
        timeType = new int[Constants.H_MAX_PIX_TIMES];
        timeLevel = new float[Constants.H_MAX_PIX_TIMES];
        timval = new float[Constants.H_MAX_PIX][Constants.H_MAX_PIX_TIMES];
        pulseSumLoc = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        pulseSumGlob = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
    }
}
