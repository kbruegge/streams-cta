package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;

/**
 * Image parameters Created by alexey on 30.06.15.
 */
public class ImgData {

    /**
     * is image data known?
     */
    boolean known;

    /**
     * Telescope ID
     */
    int telId;

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
    //TODO better naming than just L
    double l;

    /**
     * Error on length (0: error not known, <0: l not known) [rad]
     */
    double lErr;

    /**
     * Width (minor axis) [rad]
     */
    //TODO better naming than just W
    double w;

    /**
     * Error on width (0: error not known, <0: w not known) [rad]
     */
    double wErr;

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
        //TODO implement
        return false;
    }
}