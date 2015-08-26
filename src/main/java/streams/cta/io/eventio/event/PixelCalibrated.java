package streams.cta.io.eventio.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.eventio.EventIOBuffer;
import streams.cta.io.eventio.EventIOHeader;

import static streams.cta.io.eventio.EventIOConstants.H_MAX_PIX;

/**
 * Created by alexey on 30.06.15.
 */
public class PixelCalibrated {

    static Logger log = LoggerFactory.getLogger(PixelCalibrated.class);

    /**
     * is calibrated pixel data known?
     */
    int known;

    /**
     * Telescope ID
     */
    long telId;

    /**
     * Pixels in camera: list should be in this range.
     */
    int numPixels;

    /**
     * -2 (timing local peak), -1 (timing global peak), >=0 (integration scheme, if known)
     */
    int intMethod;

    /**
     * Was list of significant pixels filled in? 1: use list, 2: all pixels significant
     */
    int listKnown;

    /**
     * Size of the list of available pixels (with list mode).
     */
    int listSize;

    /**
     * List of available pixels (with list mode).
     */
    int[] pixelList;

    /**
     * Was amplitude large enough to record it?
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    byte[] significant;

    /**
     * Calibrated & flat-fielded pixel intensity [p.e.]
     */
    float[] pixelPe;

    //TODO constructor that does not need maximum values for array size
    public PixelCalibrated(int telId) {
        this.telId = telId;
        pixelList = new int[H_MAX_PIX];
        significant = new byte[H_MAX_PIX];
        pixelPe = new float[H_MAX_PIX];
    }

    public boolean readPixelCalibrated(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                known = 0;

                if (header.getVersion() > 0) {
                    log.error("Unsupported calibrated pixel intensities version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }
                telId = header.getIdentification();
                int npix = (int) buffer.readCount64();
                if (npix > H_MAX_PIX) {
                    log.error("Invalid number of pixels in calibrated pixel intensities: " + npix);
                    header.getItemEnd();
                    return false;
                }
                numPixels = npix;
                intMethod = buffer.readSCount32();
                listKnown = buffer.readSCount32();
                listSize = 0;
                if (listKnown == 2) {
                    // all pixels to be marked as significant
                    for (int ipix = 0; ipix < npix; ipix++) {
                        significant[ipix] = 1;
                    }
                } else {
                    for (int ipix = 0; ipix < npix; ipix++) {
                        significant[ipix] = 0;
                    }
                }
                if (listKnown == 1) {
                    // selected pixels by list of pixel IDs
                    listSize = buffer.readSCount32();
                    for (int i = 0; i < listSize; i++) {
                        int ipix = buffer.readSCount32();
                        pixelList[i] = ipix;
                        significant[ipix] = 1;
                    }
                    for (int i = 0; i < listSize; i++) {
                        int ipix = pixelList[i];
                        pixelPe[ipix] = buffer.readSFloat();
                    }
                } else if (listKnown == -1) {
                    // selected pixels by bit(s)
                    significant = buffer.readVectorOfBytes(numPixels);
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        if (significant[ipix] != 0) {
                            pixelPe[ipix] = buffer.readSFloat();
                        }
                    }
                } else if (listKnown == 2) {
                    // all pixels significant
                    for (int ipix = 0; ipix < numPixels; ipix++)
                        pixelPe[ipix] = buffer.readSFloat();
                }

                known = 1;
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}