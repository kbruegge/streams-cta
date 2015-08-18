package streams.cta.io.eventio.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.eventio.EventIOBuffer;
import streams.cta.io.eventio.EventIOHeader;

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
    float azimuthRaw;

    /**
     * Raw altitude angle [radians].
     */
    float altitudeRaw;

    /**
     * Azimuth corrected for pointing errors.
     */
    float azimuthCor;

    /**
     * Azimuth corrected for pointing errors.
     */
    float altitudeCor;

    /**
     * Set if raw angles are known.
     */
    boolean rawKnown;

    /**
     * Set if corrected angles are known.
     */
    boolean corKnown;

    public TrackEvent(int id) {
        telId = id;
    }

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
                    azimuthRaw = buffer.readFloat();
                    altitudeRaw = buffer.readFloat();
                }

                if (corKnown) {
                    altitudeCor = buffer.readFloat();
                    azimuthCor = buffer.readFloat();
                }
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
