package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import streams.cta.Constants;

/**
 * All data for one event
 * Created by alexey on 30.06.15.
 */
public class FullEvent {

    static Logger log = LoggerFactory.getLogger(FullEvent.class);

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
        this(Constants.H_MAX_TEL);
        numTel = 0;
        numTeldata = 0;
    }

    public FullEvent(int numberTelescopes){
        numTel = numberTelescopes;
        numTeldata = 0;
        teldata = new TelEvent[numberTelescopes];
        trackdata = new TrackEvent[numberTelescopes];
        teldataList = new int[numberTelescopes];
        shower = new ShowerParameters();
        central = new CentralEvent();
    }
}