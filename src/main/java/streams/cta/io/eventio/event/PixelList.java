package streams.cta.io.eventio.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.eventio.EventIOBuffer;
import streams.cta.io.eventio.EventIOHeader;

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
    short[] pixelList;

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
                    pixelList = buffer.readVectorOfShorts(pixels);
                } else {
                    for (int i = 0; i < pixels; i++) {
                        int value = buffer.readSCount32();
                        if (value > Short.MAX_VALUE) {
                            log.error("Pixel list values are greater than Short.MAX_VALUE.");
                        }
                        pixelList[i] = (short) value;
                    }
                    //pixelList = buffer.readVectorOfIntsScount(pixels);
                }
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
