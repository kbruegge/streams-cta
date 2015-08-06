package streams.cta.io.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Image parameters Created by alexey on 30.06.15.
 */
public class ImgData {

    static Logger log = LoggerFactory.getLogger(ImgData.class);

    /**
     * is image data known?
     */
    boolean known;

    /**
     * Telescope ID
     */
    public int telId;

    /**
     * number of pixels used for image
     */
    int pixels;

    /**
     * For which set of tail-cuts was used.
     */
    int cutId;

    /**
     * Image amplitude (="SIZE") [mean p.e.]
     */
    float amplitude;

    /**
     * Pixel amplitude clipping level [mean p.e.] or zero for no clipping.
     */
    float clipAmp;

    /**
     * Number of pixels in saturation (ADC saturation or dedicated clipping).
     */
    int numSat;

    /** Position */

    /**
     * X position (c.o.g.) [rad], corrected for any camera rotation.
     */
    float x;

    /**
     * Error on x (0: error not known, <0: x not known) [rad]
     */
    float xErr;

    /**
     * Y position (c.o.g.) [rad], corrected for any camera rotation.
     */
    float y;

    /**
     * Error on y (0: error not known, <0: y not known) [rad]
     */
    float yErr;

    /** Orientation */

    /**
     * Angle of major axis w.r.t. x axis [rad], corrected for any camera rotation.
     */
    float phi;

    /**
     * Error on phi (0: error not known, <0: phi not known) [rad]
     */
    float phiErr;


    /** Shape */

    /**
     * Length (major axis) [rad]
     */
    float length;

    /**
     * Error on length (0: error not known, <0: length not known) [rad]
     */
    float lengthErr;

    /**
     * Width (minor axis) [rad]
     */
    float width;

    /**
     * Error on width (0: error not known, <0: width not known) [rad]
     */
    float widthErr;

    /**
     * Skewness, indicating asymmetry of image
     */
    float skewness;

    /**
     * Error (0: error not known, <0: skewness not known)
     */
    float skewnessErr;

    /**
     * Kurtosis, indicating sharpness of peak of image
     */
    float kurtosis;

    /**
     * Error (0: error not known, <0: kurtosis not known)
     */
    float kurtosisErr;

    /**
     * Number of hottest pixels used for concentration
     */
    int numConc;

    /**
     * Fraction of total amplitude in numConc hottest pixels
     */
    float concentration;


    /** Timing */

    /**
     * Slope in peak times along major axis as given by phi. [ns/rad]
     */
    float tmSlope;

    /**
     * R.m.s. average residual time after slope correction. [ns]
     */
    float tmResidual;

    /**
     * Average pulse width (50% of peak or time over threshold) [ns]
     */
    float tmWidth1;

    /**
     * Average pulse width (20% of peak or 0) [ns]
     */
    float tmWidth2;

    /**
     * Average pixel rise time (or 0) [ns]
     */
    float tmRise;


    /** Individual pixels */

    /**
     * Number of hottest pixels individually saved
     */
    int numHot;

    /**
     * Pixel IDs of hotest pixels
     */
    int[] hotPixel;

    /**
     * Amplitudes of hotest pixels [mean p.e.]
     */
    float[] hotAmp;

    public ImgData(int id) {
        telId = id;
    }

    public boolean readTelImage(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                known = false;
                long version = header.getVersion();
                if (version > 6) {
                    log.error("Unsupported telescope image version: " + version);
                    header.getItemEnd();
                    return false;
                }

                /* Lots of small data was packed into the ID */
                long identification = header.getIdentification();

                if (((identification & 0xff) | ((identification & 0x3f000000) >> 16)) != telId) {
                    log.warn("Image data is for wrong telescope");
                    header.getItemEnd();
                    return false;
                }

                cutId = (int) ((identification & 0xff000) >> 12);
                // always reset it
                pixels = 0;
                numSat = 0;
                clipAmp = 0.f;
                if (version >= 6) {
                    pixels = buffer.readSCount32();
                } else if (version >= 2) {
                    pixels = buffer.readShort();
                }
                if (version >= 4) {
                    if (version >= 6) {
                        numSat = buffer.readSCount32();
                    } else {
                        numSat = buffer.readShort();
                    }
                    if (numSat > 0 && version >= 5) {
                        clipAmp = buffer.readFloat();
                    }
                }

                amplitude = buffer.readFloat();
                x = buffer.readFloat();
                y = buffer.readFloat();
                phi = buffer.readFloat();
                length = buffer.readFloat();
                width = buffer.readFloat();
                numConc = buffer.readShort();
                concentration = buffer.readFloat();

                if ((identification & 0x100) != 0) {
                    // Error estimates of 1st+2nd moments in data
                    xErr = buffer.readFloat();
                    yErr = buffer.readFloat();
                    phiErr = buffer.readFloat();
                    lengthErr = buffer.readFloat();
                    widthErr = buffer.readFloat();
                } else {
                    xErr = 0.f;
                    yErr = 0.f;
                    phiErr = 0.f;
                    lengthErr = 0.f;
                    widthErr = 0.f;
                }

                if ((identification & 0x200) != 0) {
                    // 3rd+4th moments plus errors in data
                    skewness = buffer.readFloat();
                    skewnessErr = buffer.readFloat();
                    kurtosis = buffer.readFloat();
                    kurtosisErr = buffer.readFloat();
                } else {
                    skewness = 0.f;
                    skewnessErr = -1.f;
                    kurtosis = 0.f;
                    kurtosisErr = -1.f;
                }

                if ((identification & 0x400) != 0) {
                    // ADC sum of high-intensity pixels in data
                    if (version <= 5) {
                        numHot = buffer.readShort();
                        hotAmp = buffer.readVectorOfFloats(numHot);
                        if (version >= 1) {
                            hotPixel = buffer.readVectorOfShorts(numHot);
                        }
                    } else {
                        numHot = buffer.readSCount32();
                        hotAmp = buffer.readVectorOfFloats(numHot);
                        if (version >= 1) {
                            hotPixel = buffer.readVectorOfIntsScount(numHot);
                        }
                    }
                } else {
                    numHot = 0;
                }

                if ((identification & 0x800) != 0 && version >= 3) {
                    // New in version 3: timing summary
                    tmSlope = buffer.readFloat();
                    tmResidual = buffer.readFloat();
                    tmWidth1 = buffer.readFloat();
                    tmWidth2 = buffer.readFloat();
                    tmRise = buffer.readFloat();
                } else {
                    tmSlope = 0.f;
                    tmResidual = 0.f;
                    tmWidth1 = 0.f;
                    tmWidth2 = 0.f;
                    tmRise = 0.f;
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