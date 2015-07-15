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
    long zeroSupMode;

    /**
     * The desired or used data reduction mode.
     */
    long dataRedMode;

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

    /**
     * Write ADC sum data for one camera in eventio format.
     *
     * @param buffer EventIOBuffer
     * @return true if reading was successful, false otherwise
     */
    public boolean readTelADCSums(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 3) {
                    log.error("Unsupported ADC sums version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                /* Lots of small data was packed into the ID */
                long flags = header.getIdentification();

                extractDataFromID(buffer, header, flags);

                // We have sums and not samples.
                numSamples = 0;

                if (numPixels > Constants.H_MAX_PIX || numGains > Constants.H_MAX_GAINS ||
                        ((numPixels >= 32768) && (zeroSupMode > 1)) || zeroSupMode > 2 ||
                        dataRedMode > 2) {
                    log.warn("Invalid raw data block is skipped.");
                    header.getItemEnd();
                    numPixels = 0;
                    return false;
                }

                if (dataRedMode == 2) {
                    offsetHg8 = buffer.readShort();
                    scaleHg8 = buffer.readShort();
                    if (scaleHg8 <= 0) {
                        scaleHg8 = 1;
                    }
                }

                // Without zero-suppression and data-reduction, every channel is known
                // but if either is z.s. or d.r. is on, a channel is only known
                // if marked as such in the data.
                int k;
                if (zeroSupMode == 0 && dataRedMode == 0) {
                    k = 1;
                } else {
                    k = 0;
                }
                for (int j = 0; j < numPixels; j++) {
                    significant[j] = (short) k;
                }
                for (int igains = 0; igains < numGains; igains++) {
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        adcKnown[igains][ipix] = (short) k;
                        adcSum[igains][ipix] = 0;
                    }
                }

                int mlg = 0;
                int m;
                int mhg8;
                int mhg16;
                int cflags;
                int bflags;
                long n;

                // originally uint32_t
                long[] lgval = new long[16];

                // originally uint32_t
                long[] hgval = new long[16];

                // originally uint8_t
                short[] hgval8 = new short[16];

                switch ((int) zeroSupMode) {
                    case 0:
                        // no zero suppression
                        switch ((int) dataRedMode) {
                            case 0:
                                // No data reduction
                                // Note: in this mode ADC sums are stored in the internal order,
                                // no matter how many different gains there are per PMT.
                                // In all other modes, the order is fixed (lg/hg16/hg8) and
                                // limited to two different gains per PMT. */
                                for (int i = 0; i < numGains; i++) {
                                    if (header.getVersion() < 3) {
                                        adcSum[i] = readAdcSumAsUint16(buffer, numPixels);
                                    } else {
                                        adcSum[i] = readAdcSumDifferential(buffer, numPixels);
                                    }
                                }
                                break;
                            //TODO original code: #if (H_MAX_GAINS >= 2) ???
                            case 1:
                                // Low low-gain channels were skipped (for two gains)
                                k = 0;
                                while (k < numPixels) {
                                    //TODO check why in original code vector of uint16 is read
                                    //get_vector_of_uint16( & cflags, 1, iobuf);
                                    cflags = buffer.readUnsignedShort();
                                    if (k + 16 <= numPixels) {
                                        n = 16;
                                    } else {
                                        n = numPixels - k;
                                    }
                                    for (int j = 0; j < n; j++) {
                                        if ((cflags & (1 << j)) != 0) {
                                            mlg++;
                                        }
                                    }

                                    if (header.getVersion() < 3) {
                                        if (numGains >= 2) {
                                            lgval = readAdcSumAsUint16(buffer, mlg);
                                        }
                                        // TODO check if the pointer logic is right
                                        // get_adcsum_as_uint16(&raw->adc_sum[Constants.HI_GAIN][k], n, iobuf)
                                        adcSum[Constants.HI_GAIN] = readAdcSumAsUint16(buffer, n);
                                        ;
                                    } else {
                                        if (numGains >= 2) {
                                            lgval = readAdcSumDifferential(buffer, mlg);
                                        }
                                        // TODO check if the pointer logic is right
                                        // get_adcsum_differential(&raw->adc_sum[Constants.HI_GAIN][k], n, iobuf)
                                        adcSum[Constants.HI_GAIN] = readAdcSumDifferential(buffer, n);
                                        ;
                                    }

                                    mlg = 0;
                                    for (int j = 0; j < n; j++) {
                                        if ((cflags & (1 << j)) != 0) {
                                            adcSum[Constants.LO_GAIN][k + j] = lgval[mlg++];
                                            adcKnown[Constants.LO_GAIN][k + j] = 1;
                                        } else {
                                            adcSum[Constants.LO_GAIN][k + j] = 0;
                                            adcKnown[Constants.LO_GAIN][k + j] = 0;
                                        }
                                        adcKnown[Constants.HI_GAIN][k + j] = 1;
                                        significant[k + j] = 1;
                                    }
                                    k += n;
                                }
                                break;

                            case 2: /* Width of high-gain channel can be reduced */
                                k = 0;
                                while (k < numPixels) {
                                    //TODO check why in original code vector of uint16 is read
                                    //get_vector_of_uint16( & cflags, 1, iobuf);
                                    cflags = buffer.readUnsignedShort();

                                    bflags = buffer.readUnsignedShort();

                                    mlg = mhg16 = mhg8 = 0;
                                    if (k + 16 <= numPixels) {
                                        n = 16;
                                    } else {
                                        n = numPixels - k;
                                    }
                                    for (int j = 0; j < n; j++) {
                                        if ((cflags & (1 << j)) != 0) {
                                            mlg++;
                                            mhg16++;
                                        } else if ((bflags & (1 << j)) != 0) {
                                            mhg8++;
                                        } else {
                                            mhg16++;
                                        }
                                    }

                                    if (header.getVersion() < 3) {
                                        if (numGains >= 2) {
                                            lgval = readAdcSumAsUint16(buffer, mlg);
                                        }
                                        hgval = readAdcSumAsUint16(buffer, mhg16);
                                    } else {
                                        if (numGains >= 2) {
                                            lgval = readAdcSumDifferential(buffer, mlg);
                                        }
                                        hgval = readAdcSumDifferential(buffer, mhg16);
                                    }
                                    hgval8 = buffer.readVectorOfUnsignedBytes(mhg8);
                                    //get_vector_of_uint8(hgval8, mhg8, iobuf);
                                    mlg = mhg8 = mhg16 = 0;
                                    for (int j = 0; j < n; j++) {
                                        if ((cflags & (1 << j)) != 0) {
                                            adcSum[Constants.LO_GAIN][k + j] = lgval[mlg++];
                                            adcKnown[Constants.LO_GAIN][k + j] = 1;
                                            adcSum[Constants.HI_GAIN][k + j] = hgval[mhg16++];
                                        } else {
                                            if ((bflags & (1 << j)) != 0) {
                                                adcSum[Constants.HI_GAIN][k + j] =
                                                        hgval8[mhg8++] * scaleHg8 + offsetHg8;
                                            } else {
                                                adcSum[Constants.HI_GAIN][k + j] = hgval[mhg16++];
                                            }
                                        }
                                        adcKnown[Constants.HI_GAIN][k + j] = 1;
                                        significant[k + j] = 1;
                                    }
                                    k += n;
                                }
                                break;
                            //#endif
                            default:
                                //TODO is it the default behaviour?
                                assert (false);
                        }
                        break;

                    case 1:
                    /* -------------- Zero suppression mode 1 --------------- */
                    /* Bit pattern indicates zero suppression */
                        switch ((int) dataRedMode) {
                            case 0: /* No data reduction */
                            case 1: /* Low low-gain channels were skipped (for two gains) */
                            case 2: /* Width of high-gain channel can be reduced */
                                k = 0;
                                while (k < numPixels) {
                                    if (k + 16 <= numPixels) {
                                        n = 16;
                                    } else {
                                        n = numPixels - k;
                                    }

                                    int zbits = buffer.readUnsignedShort();

                                    m = 0;
                                    mlg = 0;
                                    mhg16 = 0;
                                    mhg8 = 0;
                                    cflags = 0;
                                    bflags = 0;
                                    if (zbits > 0) {
                                        for (int j = 0; j < n; j++) {
                                            if ((zbits & (1 << j)) != 0) {
                                                m++;
                                            }
                                        }

                                        if (dataRedMode >= 1) {

                                            cflags = buffer.readUnsignedShort();

                                            if (dataRedMode == 2) {
                                                bflags = buffer.readUnsignedShort();
                                            }
                                            for (int j = 0; j < n; j++) {
                                                if ((zbits & (1 << j)) == 0) {
                                                    continue;
                                                }
                                                if ((cflags & (1 << j)) != 0) {
                                                    mlg++;
                                                    mhg16++;
                                                } else {
                                                    if (dataRedMode == 2) {
                                                        if ((bflags & (1 << j)) != 0) {
                                                            mhg8++;
                                                        } else {
                                                            mhg16++;
                                                        }
                                                    } else {
                                                        mhg16++;
                                                    }
                                                }
                                            }
                                        } else {
                                            mlg = mhg16 = m;
                                        }

                                        if (m > 0) {
                                            if (header.getVersion() < 3) {
                                                if (numGains >= 2) {
                                                    lgval = readAdcSumAsUint16(buffer, mlg);
                                                }
                                                hgval = readAdcSumAsUint16(buffer, mhg16);
                                            } else {
                                                if (numGains >= 2) {
                                                    lgval = readAdcSumDifferential(buffer, mlg);
                                                }
                                                hgval = readAdcSumDifferential(buffer, mhg16);
                                            }
                                            if (mhg8 > 0) {
                                                hgval8 = buffer.readVectorOfUnsignedBytes(mhg8);
                                            }

                                            mlg = 0;
                                            mhg16 = 0;
                                            mhg8 = 0;
                                            for (int j = 0; j < n; j++) {
                                                if ((zbits & (1 << j)) != 0) {
                                                    significant[k + j] = 1;
                                                    if (dataRedMode < 1 || (cflags & (1 << j)) != 0) {
                                                        //TODO should we use those IFs?
                                                        //#if (H_MAX_GAINS >= 2)
                                                        adcSum[Constants.LO_GAIN][k + j] = lgval[mlg++];
                                                        //#endif
                                                        adcSum[Constants.HI_GAIN][k + j] = hgval[mhg16++];
                                                        //#if (H_MAX_GAINS >= 2)
                                                        adcKnown[Constants.LO_GAIN][k + j] = 1;
                                                        //#endif
                                                        adcKnown[Constants.HI_GAIN][k + j] = 1;
                                                    } else {
                                                        //TODO should we use those IFs?
                                                        //#if (H_MAX_GAINS >= 2)
                                                        adcSum[Constants.LO_GAIN][k + j] = 0;
                                                        //#endif
                                                        if (dataRedMode == 2 && (bflags & (1 << j)) != 0) {
                                                            adcSum[Constants.HI_GAIN][k + j] =
                                                                    hgval8[mhg8++] * scaleHg8 + offsetHg8;
                                                        } else {
                                                            adcSum[Constants.HI_GAIN][k + j] = hgval[mhg16++];
                                                        }
                                                        adcKnown[Constants.HI_GAIN][k + j] = 1;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    k += n;
                                }
                                break;

                            default:
                                assert (false);
                        }
                        break;

                    case 2:
                    /* -------------- Zero suppression mode 2 --------------- */
                    /* List of not zero-suppressed pixels */
                        long[][] adcSumL = new long[Constants.H_MAX_GAINS][Constants.H_MAX_PIX];
                        boolean[] withoutLg = new boolean[Constants.H_MAX_PIX];
                        boolean[] reducedWidth = new boolean[Constants.H_MAX_PIX];

                        switch ((int) dataRedMode) {
                            case 0: /* No data reduction */
                            case 1: /* Low low-gain channels were skipped (for two gains) */
                            case 2: /* Width of high-gain channel can be reduced */
                                listSize = buffer.readShort();
                                int[] adcListL = buffer.readVectorOfInts(listSize);
                                mlg = 0;
                                mhg16 = 0;
                                mhg8 = 0;
                                for (int j = 0; j < listSize; j++) {
                                    k = adcListL[j] & 0x1fff;
                                    adcList[j] = k;
                                    withoutLg[j] = ((adcListL[j] & 0x2000) != 0);
                                    reducedWidth[j] = ((adcListL[j] & 0x4000) != 0);
                                    if (reducedWidth[j]) {
                                        mhg8++;
                                    } else if (withoutLg[j]) {
                                        mhg16++;
                                    } else {
                                        mlg++;
                                        mhg16++;
                                    }
                                }

                                if (header.getVersion() < 2) {
                                    //TODO check if we should use these IFs?
                                    //#if (H_MAX_GAINS >= 2)
                                    if (numGains >= 2) {
                                        adcSumL[Constants.LO_GAIN] = readAdcSumAsUint16(buffer, mlg);
                                    }
                                    //#endif
                                    adcSumL[Constants.HI_GAIN] = readAdcSumAsUint16(buffer, mhg16);
                                } else {
                                    //TODO check if we should use these IFs?
                                    //#if (H_MAX_GAINS >= 2)
                                    if (numGains >= 2) {
                                        adcSumL[Constants.LO_GAIN] = readAdcSumDifferential(buffer, mlg);
                                    }
                                    //#endif
                                    adcSumL[Constants.HI_GAIN] = readAdcSumDifferential(buffer, mhg16);
                                }

                                short[] adcHg8 = buffer.readVectorOfUnsignedBytes(mhg8);

                                mlg = 0;
                                mhg16 = 0;
                                mhg8 = 0;
                                for (int j = 0; j < listSize; j++) {
                                    k = adcList[j];
                                    significant[k] = 1;
                                    if (reducedWidth[j]) {
                                        adcSum[Constants.HI_GAIN][k] = adcHg8[mhg8++] * scaleHg8 + offsetHg8;
                                    } else {
                                        adcSum[Constants.HI_GAIN][k] = adcSumL[Constants.HI_GAIN][mhg16++];
                                    }
                                    adcKnown[Constants.HI_GAIN][k] = 1;
                                    //TODO check if we should use these IFs?
                                    //#if (H_MAX_GAINS >= 2)
                                    if (withoutLg[j]) {
                                        adcSum[Constants.LO_GAIN][k] = 0;
                                        adcKnown[Constants.LO_GAIN][k] = 0;
                                    } else {
                                        adcSum[Constants.LO_GAIN][k] = adcSumL[Constants.LO_GAIN][mlg++];
                                        adcKnown[Constants.LO_GAIN][k] = 1;
                                    }
                                    //#endif
                                }
                                break;

                            default:
                                assert (false);
                        }
                        break;

                    default:
                        assert (false);
                }
                known = 1;
                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    private long[] readAdcSumDifferential(EventIOBuffer buffer, long number) {
        long[] result = new long[(int) number];
        int prev = 0;
        int curr;
        for (int ipix = 0; ipix < number; ipix++) {
            //TODO originally scount32
            curr = buffer.readSCount() + prev;
            prev = curr;
            result[ipix] = curr;
        }
        return result;
    }

    /**
     * Read a vector of unsigned shorts and return them in a long-array.
     *
     * @param buffer EventIOBuffer
     * @param number number of shorts to be read
     * @return array of long type
     */
    private long[] readAdcSumAsUint16(EventIOBuffer buffer, long number) throws IOException {

        // Old format: 16-bit unsigned, good for <= 16 samples of <= 12 bits or such.
        int[] shortAdcSum = buffer.readVectorOfUnsignedShort((int) number);
        long[] result = new long[(int) number];
        for (int i = 0; i < shortAdcSum.length; i++) {
            result[i] = shortAdcSum[i];
        }
        return result;
    }

    /**
     * Extract some information out of the identification field of a header.
     *
     * @param buffer EventIOBuffer to read from data stream
     * @param header EventIOHeader to get version information
     * @param ident  identification field with some hidden information
     */
    private void extractDataFromID(EventIOBuffer buffer, EventIOHeader header, long ident)
            throws IOException {
        // TODO changed types of telId, numPixels and numGains to LONG. change back and handle it right?
        zeroSupMode = ident & 0x1f;
        dataRedMode = (ident >> 5) & 0x1f;
        listKnown = (ident >> 10) & 0x01;
        if (header.getVersion() == 0) {
            // High-order bits may be missing.
            telId = (ident >> 25) & 0x1f;
            numPixels = (ident >> 12) & 0x07ff;
            numGains = (ident >> 23) & 0x03;
        } else if (header.getVersion() == 1) {
            // High-order bits may be missing.
            telId = (ident >> 25) & 0x1f;
            numPixels = (ident >> 12) & 0x0fff;
            numGains = (((ident >> 24) & 0x01) != 0 ? 2 : 1);
        } else {
            // High-order bits may be missing.
            telId = (ident >> 12) & 0xffff;
            numPixels = buffer.readLong();
            numGains = buffer.readShort();
        }
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

                extractDataFromID(buffer, header, flags);

                if ((zeroSupMode != 0 && header.getVersion() < 3)
                        || dataRedMode != 0 || listKnown != 0) {
                    log.warn("Unsupported ADC sample format.");
                    header.getItemEnd();
                    return false;
                }

                zeroSupMode |= zeroSupMode << 5;

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
