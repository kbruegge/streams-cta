package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Lists of pixels (triggered, selected, etc.)
 *
 * @author alexey
 */
public class PixelList {

    static Logger log = LoggerFactory.getLogger(PixelList.class);

    /**
     * Indicates what sort of list this is: 0 (triggered pixel), 1 (selected pixel), ...
     */
    long code;

    /**
     * The size of the pixels in this list.
     */
    int pixels;

    /**
     * The actual list of pixel numbers.
     */
    int[] pixelList;

    public PixelList() {
        pixelList = new int[Constants.H_MAX_PIX];
    }

    public boolean readPixelList(EventIOBuffer buffer) {
        /*
         * TODO in original Berloehr uses a pointer to a telescope id which is
         * set to header.ident % 1000000, but it seems not to be used somewhere
         */
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 1) {
                    log.error("Unsupported pixel list version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }
                code = header.getIdentification() / 1000000;

                pixels = header.getVersion() < 1 ? buffer.readShort() : buffer.readSCount32();
                if (pixels > Constants.H_MAX_PIX) {
                    log.error("Got a pixel list with " + pixels
                            + " pixels but can only handle lists up to " + Constants.H_MAX_PIX);
                    pixels = 0;
                    header.getItemEnd();
                    return false;
                }

                if (header.getVersion() < 1) {
                    pixelList = buffer.readVectorOfInts(pixels);
                } else {
                    pixelList = buffer.readVectorOfIntsScount(pixels);
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
