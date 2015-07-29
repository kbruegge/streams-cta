package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Created by alexey on 30.06.15.
 */
public class PixelTiming {

    static Logger log = LoggerFactory.getLogger(PixelTiming.class);

    /**
     * is pixel timing data known?
     */
    boolean known;

    /**
     * Telescope ID
     */
    public int telId;

    /**
     * Pixels in camera: list should be in this range.
     */
    int numPixels;

    /**
     * Number of different gains per pixel.
     */
    int numGains;

    /**
     * 0: not set; 1: individual pixels; 2: pixel ranges.
     */
    int listType;

    /**
     * The size of the pixels in this list.
     */
    int listSize;

    /**
     * The actual list of pixel numbers.
     */
    int[] pixelList;

    /**
     * Minimum base-to-peak raw amplitude difference applied in pixel selection.
     */
    int threshold;

    /**
     * Number of bins before peak being summed up.
     */
    int beforePeak;

    /**
     * Number of bins after peak being summed up.
     */
    int afterPeak;

    /**
     * How many different types of times can we store?
     */
    int numTypes;

    /**
     * Which types come in which order.
     */
    int[] timeType;

    /**
     * The width and startpos types apply above some fraction from base to peak.
     */
    float[] timeLevel;

    /**
     * Actually stored are the following timvals divided by granularity, as 16-bit integers. Set
     * this to e.g. 0.25 for a 0.25 time slice stepping.
     */
    float granularity;

    /**
     * Camera-wide (mean) peak position [time slices].
     */
    float peakGlobal;

    /**
     * Only the first 'pixels' elements are actually filled and stored. Others are undefined.
     */
    float[][] timval;

    /**
     * Amplitude sum around local peak, for pixels in list. Ped. subtr. Only present if
     * before&afterPeak>=0.
     */
    int[][] pulseSumLoc;

    /**
     * Amplitude sum around global peak; for all pixels. Ped. subtracted. Only present if
     * before&afterPeak>=0 and if list is of size>0 (otherwise no peak).
     */
    int[][] pulseSumGlob;

    public boolean readPixTime(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 2) {
                    log.error("Unsupported pixel timing version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                known = false;

                listType = 1;
                listSize = 0;
                numTypes = 0;
                short v0 = 0;
                int globOnlySelected = 0;
                int withSum = 0;
                float scale;
                if (header.getVersion() == 0) {
                    v0 = 1;
                }
                if (header.getVersion() <= 1) {
                    numPixels = buffer.readShort();
                } else {
                    numPixels = buffer.readSCount32();
                }

                numGains = buffer.readShort();
                beforePeak = buffer.readShort();
                afterPeak = buffer.readShort();
                listType = buffer.readShort();
                if (listType != 1 && listType != 2) {
                    log.error("Invalid type of pixel list in pixel timing data: " + listType);
                    header.getItemEnd();
                    return false;
                }

                if (header.getVersion() <= 1) {
                    listSize = buffer.readShort();
                } else {
                    listSize = buffer.readSCount32();
                }
                if (listSize < 0 || listSize > Constants.H_MAX_PIX) {
                    log.error("Invalid size of pixel list in pixel timing data: " + listSize);
                    header.getItemEnd();
                    return false;
                }
                if (header.getVersion() <= 1) {
                    if (listType == 1) {
                        pixelList = buffer.readVectorOfInts(listSize);
                    } else {
                        pixelList = buffer.readVectorOfInts(2 * listSize);
                    }
                } else {
                    if (listType == 1) {
                        pixelList = buffer.readVectorOfIntsScount(listSize);
                    } else {
                        pixelList = buffer.readVectorOfIntsScount(2 * listSize);
                    }
                }
                threshold = buffer.readShort();
                if (threshold < 0) {
                    globOnlySelected = 1;
                }
                if (beforePeak >= 0 && afterPeak >= 0) {
                    withSum = 1;
                }
                numTypes = buffer.readShort();
                if (numTypes < 0 || numTypes > Constants.H_MAX_PIX_TIMES) {
                    log.error("Invalid number of types in pixel timing data: " + numTypes);
                    header.getItemEnd();
                    return false;
                }
                timeType = buffer.readVectorOfInts(numTypes);
                timeLevel = buffer.readVectorOfFloats(numTypes);
                granularity = buffer.readReal();
                if (granularity > 0.) {
                    scale = granularity;
                } else {
                    scale = 0.01f;
                    granularity = 0.01f;
                }
                peakGlobal = buffer.readReal();

                // initialize arrays
                timval = new float[numPixels][numTypes];
                pulseSumLoc = new int[numGains][numPixels];
                pulseSumGlob = new int[numGains][numPixels];

                // The first timing element is always initialised to indicate unknown.
                for (int i = 0; i < numPixels; i++) {
                    timval[i][0] = -1f;
                }

                for (int i = 0; i < listSize; i++) {
                    int k1, k2;
                    if (listType == 1) {
                        k1 = pixelList[i];
                        k2 = k1;
                    } else {
                        k1 = pixelList[2 * i];
                        k2 = pixelList[2 * i + 1];
                    }
                    for (int ipix = k1; ipix <= k2; ipix++) {
                        for (int j = 0; j < numTypes; j++) {
                            timval[ipix][j] = scale * buffer.readShort();
                        }
                        if (withSum != 0) {
                            for (int igain = 0; igain < numGains; igain++) {
                                pulseSumLoc[igain][ipix] = (v0 != 0 ?
                                        buffer.readShort() : buffer.readSCount32());
                            }
                            if (globOnlySelected != 0) {
                                for (int igain = 0; igain < numGains; igain++) {
                                    pulseSumGlob[igain][ipix] =
                                            (v0 != 0 ? buffer.readShort() : buffer.readSCount32());
                                }
                            }
                        }
                    }
                }

                if (withSum != 0 && listSize > 0 && globOnlySelected == 0) {
                    for (int igain = 0; igain < numGains; igain++) {
                        for (int j = 0; j < numPixels; j++) {
                            pulseSumGlob[igain][j] =
                                    (v0 != 0 ? buffer.readShort() : buffer.readSCount32());
                        }
                    }
                }
                known = true;
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
