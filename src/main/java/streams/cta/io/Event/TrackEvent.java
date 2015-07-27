package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Tracking data interpolated for one event and one telescope.
 *
 * @author alexey
 */
public class TrackEvent {

    static Logger log = LoggerFactory.getLogger(TrackEvent.class);

    /**
     * The telescope ID number (1 ... n)
     */
    public long telId;

    /**
     * Raw azimuth angle [radians from N->E].
     */
    double azimuthRaw;

    /**
     * Raw altitude angle [radians].
     */
    double altitudeRaw;

    /**
     * Azimuth corrected for pointing errors.
     */
    double azimuthCor;

    /**
     * Azimuth corrected for pointing errors.
     */
    double altitudeCor;

    /**
     * Set if raw angles are known.
     */
    boolean rawKnown;

    /**
     * Set if corrected angles are known.
     */
    boolean corKnown;

    /**
     * Read a tracking position in eventio format.
     *
     * @param buffer EventIOBuffer to read from data stream
     */
    public boolean readTrackEvent(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                long telId = (header.getIdentification() & 0xff) |
                        ((header.getIdentification() & 0x3f000000) >> 16);

                if (telId < 0 || telId != this.telId) {
                    log.warn("Not a tracking event block or one for the wrong telescope.");
                    header.getItemEnd();
                    return false;
                }
                if (header.getVersion() != 0) {
                    log.error("Unsupported tracking event version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }
                rawKnown = (header.getIdentification() & 0x100) != 0;
                corKnown = (header.getIdentification() & 0x200) != 0;

                //TODO is it right? aziRaw -> altRaw and then altCor -> aziCor
                if (rawKnown) {
                    azimuthRaw = buffer.readReal();
                    altitudeRaw = buffer.readReal();
                }

                if (corKnown) {
                    altitudeCor = buffer.readReal();
                    azimuthCor = buffer.readReal();
                }
                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
