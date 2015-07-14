package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;

/**
 * Lists of pixels (triggered, selected, etc.) Created by alexey on 30.06.15.
 */
public class PixelList {

    /**
     * Indicates what sort of list this is: 0 (triggered pixel), 1 (selected pixel), ...
     */
    int code;

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
        //TODO implement
        return false;
    }
}
