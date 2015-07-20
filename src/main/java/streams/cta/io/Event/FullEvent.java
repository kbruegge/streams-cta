package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;
import streams.cta.io.HTime;

import static streams.cta.Constants.H_MAX_TEL;
import static streams.cta.Constants.TYPE_CENTRAL_EVENT;
import static streams.cta.Constants.TYPE_SHOWER;
import static streams.cta.Constants.TYPE_TEL_EVENT;
import static streams.cta.Constants.TYPE_TRACK_EVENT;

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
        this(H_MAX_TEL);
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
    public boolean readFullEvent(EventIOBuffer buffer, int what) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                //TODO should we use the header to skip this whole event item in a case any of its subitems are failed to be read?
                if (header.getVersion() != 0) {
                    log.error("Unsupported FullEvent version: " + header.getVersion());
                    //TODO we should get item end?!
                    buffer.skipBytes((int) header.getLength());
                    return false;
                }

                long id = header.getIdentification();

                // reset time
                central.cpuTime = new HTime();
                central.gpsTime = new HTime();

                // TODO Run-Header: numTel is set somewhere before this line
                // read_hess.c: lines 2133
                // hsdata->event.num_tel = hsdata->run_header.ntel;

                // TODO initialize arrays for teldata and trackdata
                for (int i = 0; i < numTel; i++) {
                    teldata[i].known = false;
                    trackdata[i].rawKnown = false;
                    trackdata[i].corKnown = false;
                }

                shower.known = 0;

                int type = buffer.nextSubitemType();
                // TODO pay attention to the case of H_MAX_TEL > 100
                while (type > 0) {
                    if (type == TYPE_CENTRAL_EVENT) {
                        // read central event
                        if (!central.readCentralEvent(buffer)) {
                            log.error("Error reading central event.");
                            header.getItemEnd();
                            break;
                        }
                    } else if (type >= TYPE_TRACK_EVENT && type <= TYPE_TRACK_EVENT + H_MAX_TEL) {
                        // read trackevent
                        int telId = (type - TYPE_TRACK_EVENT) % 100 +
                                100 * ((type - TYPE_TRACK_EVENT) / 1000);
                        int telNumber = buffer.findTelIndex(telId);
                        if (telNumber < 0) {
                            log.warn("Telescope number out of range for tracking data.");
                            header.getItemEnd();
                            break;
                        }
                        if (!trackdata[telNumber].readTrackEvent(buffer)) {
                            log.error("Error reading track event.");
                            header.getItemEnd();
                            break;
                        }

                    } else if (type >= TYPE_TEL_EVENT && type <= TYPE_TEL_EVENT + H_MAX_TEL) {
                        // read televent
                        int telId = (type - TYPE_TEL_EVENT) % 100 + 100 * ((type - TYPE_TEL_EVENT) / 1000);
                        int telNumber = buffer.findTelIndex(telId);
                        if (telNumber < 0) {
                            log.warn("Telescope number out of range for telescope event data.");
                            header.getItemEnd();
                            break;
                        }

                        if (!teldata[telNumber].readTelEvent(buffer, what)) {
                            log.error("Error reading telescope event.");
                            header.getItemEnd();
                            break;
                        }

                        if ((numTeldata < H_MAX_TEL) && teldata[telNumber].known) {
                            teldataList[numTeldata++] = teldata[telNumber].telId;
                        }
                    } else if (type == TYPE_SHOWER) {
                        // read shower
                        //TODO use THIS header to skip THIS item if something goes wrong
                        if (!buffer.readShower()) {
                            header.getItemEnd();
                            return false;
                        }
                    } else {
                        // invalid item type.
                        log.warn("Invalid item type " + type + " in event " + id + ".");
                        log.warn("Next subitem ident " + buffer.nextSubitemIdent());
                        header.getItemEnd();
                        return false;
                    }

                    // look up the next item and rewind back
                    type = buffer.nextSubitemType();
                }

                if (central.numTelTriggered == 0 && central.teltrgPattern != 0) {
                    listOfTelescopesToCentralEvent(buffer);
                }

                if (numTel > 0 && central.numTelTriggered == 0 && central.numTelData == 0) {
                    replicateForMonoData();
                }

                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    /**
     * Fill in the list of telescopes not present in earlier versions of the central trigger block.
     * Assumes only triggered telescopes are actually read out or that the array has no more than 16
     * telescopes.
     */
    private void listOfTelescopesToCentralEvent(EventIOBuffer buffer) {
        int nt = 0, nd = 0;
        if (numTel <= 16) {
            // For small arrays we have all the information in the bitmasks.
            for (int itel = 0; itel < numTel && itel < 16; itel++) {
                if ((central.teltrgPattern & (1 << itel)) != 0) {
                    // Not available, set to zero
                    central.teltrgTime[nt] = 0.f;
                    central.teltrgList[nt] = teldata[itel].telId;
                    nt++;
                }
                if ((central.teldataPattern & (1 << itel)) != 0) {
                    central.teldataList[nd] = teldata[itel].telId;
                    nd++;
                }
            }
        } else {
            // For larger arrays we assume only triggered telescopes were read out.
            for (int j = 0; j < numTeldata; j++) {
                int telId = teldataList[j];
                int itel = buffer.findTelIndex(telId);
                if (itel < 0) {
                    continue;
                }
                if (teldata[itel].known) {
                    central.teltrgTime[nt] = 0.f;
                    central.teltrgList[nt++] = teldata[itel].telId;
                    central.teldataList[nd++] = teldata[itel].telId;
                }
            }
        }
        central.numTelTriggered = nt;
        central.numTelData = nd;
    }

    /**
     * Some programs may require basic central trigger data even for mono data where historically no
     * such data was stored. Replicate from the list of telescopes with data.
     */
    private void replicateForMonoData() {
        int k = 0;

        // Reconstruct basic data in central trigger block
        for (int j = 0; j < numTel; j++) {
            if (teldata[j].known) {
                central.teltrgTypeMask[k] = 0;
                central.teltrgTime[k] = 0.f;
                central.teltrgList[k] = teldata[j].telId;
                central.teldataList[k] = teldata[j].telId;
                k++;
            }
        }
        central.numTelTriggered = k;
        central.numTelData = k;

        // Recovered central data is identified by zero time values.
        central.cpuTime.resetTime();
        central.gpsTime.resetTime();
    }
}