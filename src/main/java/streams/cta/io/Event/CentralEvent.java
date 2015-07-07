package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import streams.cta.Constants;
import streams.cta.io.HTime;

/**
 * Created by alexey on 30.06.15.
 */
public /**
 * Central trigger event data
 */
class CentralEvent {

    static Logger log = LoggerFactory.getLogger(CentralEvent.class);

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
