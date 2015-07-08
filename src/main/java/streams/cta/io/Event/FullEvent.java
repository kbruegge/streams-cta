package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;
import streams.cta.io.HTime;

/**
 * All data for one event Created by alexey on 30.06.15.
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

    public FullEvent(int numberTelescopes) {
        numTel = numberTelescopes;
        numTeldata = 0;
        teldata = new TelEvent[numberTelescopes];
        trackdata = new TrackEvent[numberTelescopes];
        teldataList = new int[numberTelescopes];
        shower = new ShowerParameters();
        central = new CentralEvent();
    }

    /**
     *
     */
    public FullEvent readFullEvent(EventIOBuffer buffer, EventIOHeader header) {
        //TODO should we use the header to skip this whole event item in a case any of its subitems are failed to be read?
        if (header.getVersion() != 0) {
            log.error("Unsupported FullEvent version: " + header.getVersion());
            buffer.skipBytes(header.getLength());
            return null;
        }

        long id = header.getIdentification();

        // reset time
        central.cpuTime = new HTime();
        central.gpsTime = new HTime();

        // TODO numTel is set somewhere before this line
        // read_hess.c: lines 2133
        // hsdata->event.num_tel = hsdata->run_header.ntel;
        for (int i = 0; i < numTel; i++) {
            teldata[i].known = false;
            trackdata[i].rawKnown = false;
            trackdata[i].corKnown = false;
        }

        shower.known = 0;

        int type = buffer.nextSubitemType();
        // TODO pay attention to the case of H_MAX_TEL > 100
        while (type > 0) {
            if (type == Constants.TYPE_CENTRAL_EVENT) {
                // read central event
                central.readCentralEvent(buffer);
            } else if (type >= Constants.TYPE_TRACK_EVENT &&
                    type <= Constants.TYPE_TRACK_EVENT + Constants.H_MAX_TEL) {
                // read trackevent
                int telId = (type - Constants.TYPE_TRACK_EVENT) % 100 +
                        100 * ((type - Constants.TYPE_TRACK_EVENT) / 1000);
                int telNumber = buffer.findTelIndex(telId);
                if (telNumber < 0) {
                    log.warn("Telescope number out of range for tracking data.");
                    header.getItemEnd();
                    break;
                }
                if(!trackdata[telNumber].readTrackEvent(buffer)){
                    log.error("Error reading track event.");
                    header.getItemEnd();
                    break;
                }

            } else if (type >= Constants.TYPE_TEL_EVENT &&
                    type <= Constants.TYPE_TEL_EVENT + Constants.H_MAX_TEL) {
                // read televent
                int telId = (type - Constants.TYPE_TEL_EVENT) % 100 +
                        100 * ((type - Constants.TYPE_TEL_EVENT) / 1000);
                int telNumber = buffer.findTelIndex(telId);
                if (telNumber < 0) {
                    log.warn("Telescope number out of range for telescope event data.");
                    header.getItemEnd();
                    break;
                }

                if(!teldata[telNumber].readTelEvent(buffer)){
                    log.error("Error reading telescope event.");
                    header.getItemEnd();
                    break;
                }

                if ((numTeldata < Constants.H_MAX_TEL) && teldata[telNumber].known) {
                    teldataList[numTeldata++] = teldata[telNumber].telId;
                }
            } else if (type == Constants.TYPE_SHOWER) {
                // read shower
                //TODO use THIS header to skip THIS item if something goes wrong
                buffer.readShower();
            } else {
                // invalid item type.
                // TODO skip item
            }

            // look up the next item and rewind back
            type = buffer.nextSubitemType();
        }

        // fill the event items with some further data information

        // TODO return wright value, or NULL if something went wrong and check for it as caller
        return this;
    }
}