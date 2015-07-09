package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;

/**
 * ADC data (either sampled or sum mode) Created by alexey on 30.06.15.
 */
public class AdcData {

    /**
     * Must be set to 1 if and only if raw data is available.
     */
    int known;

    /**
     * Must match the expected telescope ID when reading.
     */
    int telId;

    /**
     * The number of pixels in the camera (as in configuration)
     */
    int numPixels;

    /**
     * The number of different gains per pixel (2 for HESS).
     */
    int numGains;

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
    int listKnown;

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

    public void resetAdc() {
        //TODO implement
    }

    public boolean readTelACSSamples(EventIOBuffer buffer, int what) {
        //TODO implement
        return false;
    }
}
