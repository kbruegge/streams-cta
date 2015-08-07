package streams.cta.io.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

import static streams.cta.Constants.HI_GAIN;
import static streams.cta.Constants.H_MAX_GAINS;
import static streams.cta.Constants.LO_GAIN;

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
    public long telId;

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
    boolean listKnown;

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
    boolean[][] adcKnown;

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
    public short[][][] adcSample;

    public AdcData(int id) {
        telId = id;
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

                //TODO remove constant magic numbers with Constants
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

                significant = new short[(int) numPixels];
                adcKnown = new boolean[(int) numGains][(int) numPixels];
                adcSum = new long[(int) numGains][(int) numPixels];

                // Without zero-suppression and data-reduction, every channel is known
                // but if either is z.s. or d.r. is on, a channel is only known
                // if marked as such in the data.
                boolean known = zeroSupMode == 0 && dataRedMode == 0;
                short signific = (short) (known ? 1 : 0);
                for (int j = 0; j < numPixels; j++) {
                    significant[j] = signific;
                }

                for (int igains = 0; igains < numGains; igains++) {
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        adcKnown[igains][ipix] = known;
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
                            case 1:
                                if (H_MAX_GAINS >= 2) {
                                    // Low low-gain channels were skipped (for two gains)
                                    int k = 0;
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
                                            adcSum[HI_GAIN] = readAdcSumAsUint16(buffer, n);
                                            ;
                                        } else {
                                            if (numGains >= 2) {
                                                lgval = readAdcSumDifferential(buffer, mlg);
                                            }
                                            // TODO check if the pointer logic is right
                                            // get_adcsum_differential(&raw->adc_sum[HI_GAIN][k], n, iobuf)
                                            adcSum[HI_GAIN] = readAdcSumDifferential(buffer, n);
                                            ;
                                        }

                                        mlg = 0;
                                        for (int j = 0; j < n; j++) {
                                            if ((cflags & (1 << j)) != 0) {
                                                adcSum[LO_GAIN][k + j] = lgval[mlg++];
                                                adcKnown[LO_GAIN][k + j] = true;
                                            } else {
                                                adcSum[LO_GAIN][k + j] = 0;
                                                adcKnown[LO_GAIN][k + j] = false;
                                            }
                                            significant[k + j] = 1;
                                            adcKnown[HI_GAIN][k + j] = true;
                                        }
                                        k += n;
                                    }
                                    break;
                                }
                            case 2: /* Width of high-gain channel can be reduced */
                                if (H_MAX_GAINS >= 2) {
                                    int k = 0;
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
                                                adcSum[LO_GAIN][k + j] = lgval[mlg++];
                                                adcKnown[LO_GAIN][k + j] = true;
                                                adcSum[HI_GAIN][k + j] = hgval[mhg16++];
                                            } else {
                                                if ((bflags & (1 << j)) != 0) {
                                                    adcSum[HI_GAIN][k + j] =
                                                            hgval8[mhg8++] * scaleHg8 + offsetHg8;
                                                } else {
                                                    adcSum[HI_GAIN][k + j] = hgval[mhg16++];
                                                }
                                            }
                                            significant[k + j] = 1;
                                            adcKnown[HI_GAIN][k + j] = true;
                                        }
                                        k += n;
                                    }
                                    break;
                                }
                            default:
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
                                int k = 0;
                                while (k < numPixels) {
                                    if (k + 16 <= numPixels) {
                                        n = 16;
                                    } else {
                                        n = numPixels - k;
                                    }

                                    int zbits = buffer.readUnsignedShort();

                                    //TODO find out what is the meaning of those variables
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
                                            mlg = m;
                                            mhg16 = m;
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
                                                        if (H_MAX_GAINS >= 2) {
                                                            adcSum[LO_GAIN][k + j] = lgval[mlg++];
                                                        }
                                                        adcSum[HI_GAIN][k + j] = hgval[mhg16++];
                                                        if (H_MAX_GAINS >= 2) {
                                                            adcKnown[LO_GAIN][k + j] = true;
                                                        }
                                                        adcKnown[HI_GAIN][k + j] = true;
                                                    } else {
                                                        if (H_MAX_GAINS >= 2) {
                                                            adcSum[LO_GAIN][k + j] = 0;
                                                        }
                                                        if (dataRedMode == 2 && (bflags & (1 << j)) != 0) {
                                                            adcSum[HI_GAIN][k + j] =
                                                                    hgval8[mhg8++] * scaleHg8 + offsetHg8;
                                                        } else {
                                                            adcSum[HI_GAIN][k + j] = hgval[mhg16++];
                                                        }
                                                        adcKnown[HI_GAIN][k + j] = true;
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
                        switch ((int) dataRedMode) {
                            case 0: /* No data reduction */
                            case 1: /* Low low-gain channels were skipped (for two gains) */
                            case 2: /* Width of high-gain channel can be reduced */
                                listSize = buffer.readShort();
                                long[][] adcSumL = new long[Constants.H_MAX_GAINS][listSize];
                                boolean[] withoutLg = new boolean[listSize];
                                boolean[] reducedWidth = new boolean[listSize];
                                adcList = new int[listSize];

                                short[] adcListL = buffer.readVectorOfShorts(listSize);
                                mlg = 0;
                                mhg16 = 0;
                                mhg8 = 0;
                                for (int j = 0; j < listSize; j++) {
                                    int k = adcListL[j] & 0x1fff;
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
                                    if (H_MAX_GAINS >= 2) {
                                        if (numGains >= 2) {
                                            adcSumL[LO_GAIN] = readAdcSumAsUint16(buffer, mlg);
                                        }
                                    }
                                    adcSumL[HI_GAIN] = readAdcSumAsUint16(buffer, mhg16);
                                } else {
                                    if (H_MAX_GAINS >= 2) {
                                        if (numGains >= 2) {
                                            adcSumL[LO_GAIN] = readAdcSumDifferential(buffer, mlg);
                                        }
                                    }
                                    adcSumL[HI_GAIN] = readAdcSumDifferential(buffer, mhg16);
                                }

                                short[] adcHg8 = buffer.readVectorOfUnsignedBytes(mhg8);

                                mlg = 0;
                                mhg16 = 0;
                                mhg8 = 0;
                                for (int j = 0; j < listSize; j++) {
                                    int k = adcList[j];
                                    significant[k] = 1;
                                    if (reducedWidth[j]) {
                                        adcSum[HI_GAIN][k] = adcHg8[mhg8++] * scaleHg8 + offsetHg8;
                                    } else {
                                        adcSum[HI_GAIN][k] = adcSumL[HI_GAIN][mhg16++];
                                    }
                                    adcKnown[HI_GAIN][k] = true;
                                    if (H_MAX_GAINS >= 2) {
                                        if (withoutLg[j]) {
                                            adcSum[LO_GAIN][k] = 0;
                                            adcKnown[LO_GAIN][k] = false;
                                        } else {
                                            adcSum[LO_GAIN][k] = adcSumL[LO_GAIN][mlg++];
                                            adcKnown[LO_GAIN][k] = true;
                                        }
                                    }
                                }
                                break;

                            default:
                                assert (false);
                        }
                        break;

                    default:
                        assert (false);
                }
                this.known = 1;
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    private long[] readAdcSumDifferential(EventIOBuffer buffer, long number) throws IOException {
        long[] result = new long[(int) number];
        int prev = 0;
        int curr;
        for (int ipix = 0; ipix < number; ipix++) {
            curr = buffer.readSCount32() + prev;
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
        for (int i = 0; i < number; i++) {
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
        listKnown = ((ident >> 10) & 0x01) != 0;
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
        listKnown = false;
        listSize = 0;

        significant = new short[(int) numPixels];
        adcKnown = new boolean[(int) numGains][(int) numPixels];
        adcSum = new long[(int) numGains][(int) numPixels];
        adcSample = new short[(int) numGains][(int) numPixels][numSamples];
    }

    public boolean readTelACSSamples(EventIOBuffer buffer, int what) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                long version = header.getVersion();
                if (version > 3) {
                    log.error("Unsupported ADC samples version: " + version);
                    header.getItemEnd();
                    return false;
                }

                // Lots of small data was packed into the ID
                // TODO originally flags is uint32_t and the other three are ints
                long flags = header.getIdentification();

                extractDataFromID(buffer, header, flags);

                if ((zeroSupMode != 0 && version < 3)
                        || dataRedMode != 0 || listKnown) {
                    log.warn("Unsupported ADC sample format.");
                    header.getItemEnd();
                    return false;
                }

                zeroSupMode |= zeroSupMode << 5;

                numSamples = buffer.readShort();

                if (numPixels > Constants.H_MAX_PIX || numGains > H_MAX_GAINS
                        || numSamples > Constants.H_MAX_SLICES) {
                    log.warn("Invalid raw data block is skipped.");
                    header.getItemEnd();
                    numPixels = 0;
                    return false;
                }

                // initialize adcSample array
                adcSample = new short[(int) numGains][(int) numPixels][numSamples];
                adcKnown = new boolean[(int) numGains][(int) numPixels];
                adcSum = new long[(int) numGains][(int) numPixels];
                significant = new short[(int) numPixels];

                if (zeroSupMode != 0) {

                    // Clear sample mode significance bits
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        significant[ipix] &= ~0xe0;
                    }

                    int listSize = buffer.readSCount32();

                    if (listSize > Constants.H_MAX_PIX) {
                        log.warn("Pixel list too large in zero-suppressed sample-mode data.");
                        header.getItemEnd();
                        return false;
                    }

                    int[][] pixelList = new int[listSize][2];

                    for (int ilist = 0; ilist < listSize; ilist++) {
                        int ipix1 = buffer.readSCount32();
                        int ipix2 = 0;
                        if (ipix1 < 0) {
                            // Single pixel
                            // TODO does it makes sense?
                            ipix2 = -ipix1 - 1;
                            ipix1 = ipix2;
                        } else {
                            // pixel range
                            ipix2 = buffer.readSCount32();
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
                                if (!adcKnown[igain][ipix]) {
                                    if ((what & Constants.RAWSUM_FLAG) != 0) {
                                        // Sum up all samples
                                        int sum = 0;
                                        for (int isamp = 0; isamp < numSamples; isamp++) {
                                            sum += adcSample[igain][ipix][isamp];
                                        }

                                        // No overflow of 32-bit unsigned assumed
                                        adcSum[igain][ipix] = sum;

                                        adcKnown[igain][ipix] = true;
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
                            if (version < 3) {
                                for (int isample = 0; isample < numSamples; isample++) {
                                    //adcSample[igain][ipix] = buffer.readVectorOfUnsignedShort(numSamples);
                                    int value = buffer.readUnsignedShort();
                                    // TODO check if the SHORT cast is ok
                                    if (value > Short.MAX_VALUE) {
                                        log.error("ADC Sample contains unsigned short values.");
                                    }
                                    adcSample[igain][ipix][isample] = (short) value;
                                }
                            } else {
                                adcSample[igain][ipix] = readAdcSampleDifferential(buffer, numSamples);
                            }

                            // Should the sampled data be summed up here? If there is preceding
                            // sum data, we keep that. Note that having non-zero-suppressed
                            // samples after sum data is normally used.In realistic data,
                            // there will be no sum known at this point.
                            if (!adcKnown[igain][ipix]) {
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
                                adcKnown[igain][ipix] = true;
                            }
                        }
                    }
                    for (int ipix = 0; ipix < numPixels; ipix++) {
                        significant[ipix] = 1;
                    }
                }

                known |= 2;

                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    private short[] readAdcSampleDifferential(EventIOBuffer buffer, int numSamples)
            throws IOException {
        // New format: store as variable-size integers.
        int prevAmp = 0;
        int thisAmp;
        short[] adcSample = new short[numSamples];
        for (int ibin = 0; ibin < numSamples; ibin++) {
            thisAmp = buffer.readSCount32() + prevAmp;

            // TODO check if the SHORT cast is ok
            if (thisAmp > Short.MAX_VALUE) {
                log.error("ADC Sample data contains unsigned short values.");
            }
            adcSample[ibin] = (short) thisAmp;
            prevAmp = thisAmp;
        }
        return adcSample;
    }
}
