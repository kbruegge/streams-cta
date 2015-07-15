package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * ADC data (either sampled or sum mode) Created by alexey on 30.06.15.
 */
public class AdcData {

    static Logger log = LoggerFactory.getLogger(AdcData.class);

    /**
     * Must be set to 1 if and only if raw data is available.
     */
    int known;

    /**
     * Must match the expected telescope ID when reading.
     */
    long telId;

    /**
     * The number of pixels in the camera (as in configuration)
     */
    long numPixels;

    /**
     * The number of different gains per pixel (2 for HESS).
     */
    long numGains;

    /**
     * The number of samples (time slices) recorded.
     */
    int numSamples;

    /**
     * The desired or used zero suppression mode.
     */
    int zeroSupMode;

    /**
     * The desired or used data reduction mode.
     */
    int dataRedMode;

    /**
     * The offset to be used in shrinking high-gain data.
     */
    int offsetHg8;

    /**
     * The scale factor (denominator) in shrinking h-g data.
     */
    int scaleHg8;

    /**
     * Threshold (in high gain) for recording low-gain data.
     */
    int threshold;

    /**
     * Was list of significant pixels filled in?
     */
    long listKnown;

    /**
     * Size of the list of available pixels (with list mode).
     */
    int listSize;

    /**
     * List of available pixels (with list mode).
     */
    int[] adcList;

    /**
     * Was amplitude large enough to record it? Bit 0: sum, 1: samples. uint8_t
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    short[] significant;

    /**
     * Was individual channel recorded? Bit 0: sum, 1: samples, 2: ADC was in saturation. uint8_t
     */
    // TODO use SHORT or maybe BYTE but using 'byteValue & 0xff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    short[][] adcKnown;

    /**
     * Sum of ADC values. uint32_t
     */
    // TODO use LONG or maybe INT but using 'intValue & 0xffffffff' for
    // arithmetic operations? http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    long[][] adcSum;

    /**
     * Pulses sampled. uint16_t
     */
    // TODO use INT or maybe SHORT but using 'intValue & 0xffff' for arithmetic operations?
    // http://stackoverflow.com/questions/16809009/using-char-as-an-unsigned-16-bit-value-in-java
    // http://jessicarbrown.com/resources/unsignedtojava.html
    int[][][] adcSample;

    //TODO constructor that does not need the max values for the arrays?!
    public AdcData() {
        adcList = new int[Constants.H_MAX_PIX];
        significant = new short[Constants.H_MAX_PIX];
        adcKnown = new short[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        adcSum = new long[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
        adcSample = new int[Constants.H_MAX_GAINS][Constants.H_MAX_PIX][Constants.H_MAX_SLICES];
    }

    public boolean readTelADCSums(EventIOBuffer buffer) {
        //TODO implement
        return false;
    }

    /**
     * Reset all values to 0 for ADC.
     */
    public void resetAdc() {
        known = 0;
        listKnown = 0;
        listSize = 0;

        for (int igain = 0; igain < numGains; igain++) {
            for (int ipix = 0; ipix < numPixels; ipix++) {
                significant[ipix] = 0;
                adcKnown[igain][ipix] = 0;
                adcSum[igain][ipix] = 0;
                for (int isample = 0; isample < numSamples; isample++) {
                    adcSample[igain][ipix][isample] = 0;
                }
            }
        }
    }

    public boolean readTelACSSamples(EventIOBuffer buffer, int what) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 3) {
                    log.error("Unsupported ADC samples version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                /* Lots of small data was packed into the ID */
                // TODO originally flags is uint32_t and the other three are ints
                long flags = header.getIdentification();
                long zeroSupMode = flags & 0x1f;
                long dataRedMode = (flags >> 5) & 0x1f;
                long listKnown = (flags >> 10) & 0x01;

                if ((zeroSupMode != 0 && header.getVersion() < 3)
                        || dataRedMode != 0 || listKnown != 0) {
                    log.warn("Unsupported ADC sample format.");
                    header.getItemEnd();
                    return false;
                }

                zeroSupMode |= zeroSupMode << 5;
                this.listKnown = listKnown;

                // TODO changed types of telId, numPixels and numGains to LONG. change back and handle it right?
                if (header.getVersion() == 0) {
                    telId = (flags >> 25) & 0x1f;
                    numPixels = (flags >> 12) & 0x07ff;
                    numGains = (flags >> 23) & 0x03;
                } else if (header.getVersion() == 1) {
                    telId = (flags >> 25) & 0x1f;
                    numPixels = (flags >> 12) & 0x0fff;
                    numGains = (((flags >> 24) & 0x01) != 0 ? 2 : 1);
                } else {
                    telId = (flags >> 12) & 0xffff;
                    numPixels = buffer.readLong();
                    numGains = buffer.readLong();
                }

                numSamples = buffer.readShort();

                if (numPixels > Constants.H_MAX_PIX || numGains > Constants.H_MAX_GAINS
                        || numSamples > Constants.H_MAX_SLICES) {
                    log.warn("Invalid raw data block is skipped.");
                    header.getItemEnd();
                    numPixels = 0;
                    return false;
                }

                if (zeroSupMode != 0) {
                    int[][] pixelList = new int[Constants.H_MAX_PIX][2];

                    // Clear sample mode significance bits
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        significant[ipix] &= ~0xe0;
                    }

                    //TODO originally readScount32
                    int listSize = buffer.readSCount();

                    if (listSize > Constants.H_MAX_PIX) {
                        log.warn("Pixel list too large in zero-suppressed sample-mode data.");
                        header.getItemEnd();
                        return false;
                    }

                    for (int ilist = 0; ilist < listSize; ilist++) {
                        int ipix1 = buffer.readSCount();
                        int ipix2 = 0;
                        if (ipix1 < 0) {
                            // Single pixel
                            // TODO does it makes sense?
                            ipix2 = -ipix1 - 1;
                            ipix1 = ipix2;
                        } else {
                            // pixel range
                            ipix2 = buffer.readSCount();
                        }

                        pixelList[ilist][0] = ipix1;
                        pixelList[ilist][1] = ipix2;
                    }

                    for (int igain = 0; igain < numGains; igain++) {
                        for (int ilist = 0; ilist < listSize; ilist++) {
                            for (int ipix = pixelList[ilist][0]; ipix <= pixelList[ilist][1]; ipix++) {
                                adcSample[igain][ipix] = readAdcSampleDifferential(buffer, numSamples);
                                significant[ipix] |= 0x20;

                                // Should the sampled data also be summed up here? There might
                                // be sum data preceding this sample mode data!
                                if (adcKnown[igain][ipix] == 0) {
                                    if ((what & Constants.RAWSUM_FLAG) != 0) {
                                        // Sum up all samples
                                        int sum = 0;
                                        for (int isamp = 0; isamp < numSamples; isamp++) {
                                            sum += adcSample[igain][ipix][isamp];
                                        }

                                        // No overflow of 32-bit unsigned assumed
                                        adcSum[igain][ipix] = sum;

                                        adcKnown[igain][ipix] = 1;
                                    } else {
                                        adcSum[igain][ipix] = 0;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int igain = 0; igain < numGains; igain++) {
                        for (int ipix = 0; ipix < numPixels; ipix++) {
                            if (header.getVersion() < 3) {
                                adcSample[igain][ipix] = buffer.readVectorOfUnsignedShort(numSamples);
                            } else {
                                adcSample[igain][ipix] = readAdcSampleDifferential(buffer, numSamples);
                            }

                            // Should the sampled data be summed up here? If there is preceding
                            // sum data, we keep that. Note that having non-zero-suppressed
                            // samples after sum data is normally used.In realistic data,
                            // there will be no sum known at this point.
                            if (adcKnown[igain][ipix] == 0) {
                                if ((what & Constants.RAWSUM_FLAG) != 0) {
                                    // Sum up all samples
                                    int sum = 0;
                                    for (int isamp = 0; isamp < numSamples; isamp++)
                                        sum += adcSample[igain][ipix][isamp];

                                    // No overflow of 32-bit unsigned assumed
                                    adcSum[igain][ipix] = sum;
                                } else {
                                    adcSum[igain][ipix] = 0;
                                }
                                adcKnown[igain][ipix] = 1;
                            }
                        }
                    }
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        significant[ipix] = 1;
                    }
                }

                known |= 2;

                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    private int[] readAdcSampleDifferential(EventIOBuffer buffer, int numSamples) {
        /* New format: store as variable-size integers. */
        int ibin;
        int prevAmp = 0, thisAmp;
        int[] adcSample = new int[numSamples];
        for (ibin = 0; ibin < numSamples; ibin++) {
            //TODO originally readSCount32
            thisAmp = buffer.readSCount() + prevAmp;
            adcSample[ibin] = thisAmp;
            prevAmp = thisAmp;
        }
        return adcSample;
    }
}
