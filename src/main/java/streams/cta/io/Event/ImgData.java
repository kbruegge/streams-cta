package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
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
    double amplitude;

    /**
     * Pixel amplitude clipping level [mean p.e.] or zero for no clipping.
     */
    double clipAmp;

    /**
     * Number of pixels in saturation (ADC saturation or dedicated clipping).
     */
    int numSat;

    /** Position */

    /**
     * X position (c.o.g.) [rad], corrected for any camera rotation.
     */
    double x;

    /**
     * Error on x (0: error not known, <0: x not known) [rad]
     */
    double xErr;

    /**
     * Y position (c.o.g.) [rad], corrected for any camera rotation.
     */
    double y;

    /**
     * Error on y (0: error not known, <0: y not known) [rad]
     */
    double yErr;

    /** Orientation */

    /**
     * Angle of major axis w.r.t. x axis [rad], corrected for any camera rotation.
     */
    double phi;

    /**
     * Error on phi (0: error not known, <0: phi not known) [rad]
     */
    double phiErr;


    /** Shape */

    /**
     * Length (major axis) [rad]
     */
    double length;

    /**
     * Error on length (0: error not known, <0: length not known) [rad]
     */
    double lengthErr;

    /**
     * Width (minor axis) [rad]
     */
    double width;

    /**
     * Error on width (0: error not known, <0: width not known) [rad]
     */
    double widthErr;

    /**
     * Skewness, indicating asymmetry of image
     */
    double skewness;

    /**
     * Error (0: error not known, <0: skewness not known)
     */
    double skewnessErr;

    /**
     * Kurtosis, indicating sharpness of peak of image
     */
    double kurtosis;

    /**
     * Error (0: error not known, <0: kurtosis not known)
     */
    double kurtosisErr;

    /**
     * Number of hottest pixels used for concentration
     */
    int numConc;

    /**
     * Fraction of total amplitude in numConc hottest pixels
     */
    double concentration;


    /** Timing */

    /**
     * Slope in peak times along major axis as given by phi. [ns/rad]
     */
    double tmSlope;

    /**
     * R.m.s. average residual time after slope correction. [ns]
     */
    double tmResidual;

    /**
     * Average pulse width (50% of peak or time over threshold) [ns]
     */
    double tmWidth1;

    /**
     * Average pulse width (20% of peak or 0) [ns]
     */
    double tmWidth2;

    /**
     * Average pixel rise time (or 0) [ns]
     */
    double tmRise;


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
    double[] hotAmp;

    //TODO constructor that does not need max values for array size
    public ImgData() {
        hotPixel = new int[Constants.H_MAX_HOTPIX];
        hotAmp = new double[Constants.H_MAX_HOTPIX];
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
                clipAmp = 0.;
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
                        clipAmp = buffer.readReal();
                    }
                }

                amplitude = buffer.readReal();
                x = buffer.readReal();
                y = buffer.readReal();
                phi = buffer.readReal();
                length = buffer.readReal();
                width = buffer.readReal();
                numConc = buffer.readShort();
                concentration = buffer.readReal();

                if ((identification & 0x100) != 0) {
                    // Error estimates of 1st+2nd moments in data
                    xErr = buffer.readReal();
                    yErr = buffer.readReal();
                    phiErr = buffer.readReal();
                    lengthErr = buffer.readReal();
                    widthErr = buffer.readReal();
                } else {
                    xErr = 0.;
                    yErr = 0.;
                    phiErr = 0.;
                    lengthErr = 0.;
                    widthErr = 0.;
                }

                if ((identification & 0x200) != 0) {
                    // 3rd+4th moments plus errors in data
                    skewness = buffer.readReal();
                    skewnessErr = buffer.readReal();
                    kurtosis = buffer.readReal();
                    kurtosisErr = buffer.readReal();
                } else {
                    skewness = 0.;
                    skewnessErr = -1.;
                    kurtosis = 0.;
                    kurtosisErr = -1.;
                }

                if ((identification & 0x400) != 0) {
                    // ADC sum of high-intensity pixels in data
                    if (version <= 5) {
                        numHot = buffer.readShort();
                        hotAmp = buffer.readVectorOfReals(numHot);
                        if (version >= 1) {
                            hotPixel = buffer.readVectorOfInts(numHot);
                        }
                    } else {
                        numHot = buffer.readSCount32();
                        hotAmp = buffer.readVectorOfReals(numHot);
                        if (version >= 1) {
                            hotPixel = buffer.readVectorOfIntsScount(numHot);
                        }
                    }
                } else {
                    numHot = 0;
                }

                if ((identification & 0x800) != 0 && version >= 3) {
                    // New in version 3: timing summary
                    tmSlope = buffer.readReal();
                    tmResidual = buffer.readReal();
                    tmWidth1 = buffer.readReal();
                    tmWidth2 = buffer.readReal();
                    tmRise = buffer.readReal();
                } else {
                    tmSlope = 0.;
                    tmResidual = 0.;
                    tmWidth1 = 0.;
                    tmWidth2 = 0.;
                    tmRise = 0.;
                }

                known = true;
                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}