package streams.cta.io.Event;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;

/**
 * Reconstructed shower parameters. Created by alexey on 30.06.15.
 */
public class ShowerParameters {
    int known;
    int numTrg;      ///< Number of telescopes contributing to central trigger.
    int numRead;     ///< Number of telescopes read out.
    int numImg;      ///< Number of images used for shower parameters.
    int imgPattern;  ///< Bit pattern of which telescopes were used (for small no. of telescopes only).
    int[] imgList; ///< With more than 16 or 32 telescopes, we can only use the list.

    /**
     * Bit pattern of what results are available: Bits 0 + 1: direction + errors Bits 2 + 3: core
     * position + errors Bits 4 + 5: mean scaled image shape + errors Bits 6 + 7: energy + error
     * Bits 8 + 9: shower maximum + error
     */
    int resultBits;

    double azimuthAngle;        ///< Azimuth angle [radians from N->E]
    double altitude;       ///< Altitude [radians]
    double errDir1;  ///< Error estimate in nominal plane X direction (|| Alt) [rad]
    double errDir2;  ///< Error estimate in nominal plane Y direction (|| Az) [rad]
    double errDir3;  ///< ?
    double xc;        ///< X core position [m]
    double yc;        ///< Y core position [m]
    double errCore1; ///< Error estimate in X coordinate [m]
    double errCore2; ///< Error estimate in Y coordinate [m]
    double errCore3; ///< ?
    double mscl;      ///< Mean scaled image length [gammas ~1 (HEGRA-style) or ~0 (HESS-style)].
    double errMscl;
    double mscw;      ///< Mean scaled image width [gammas ~1 (HEGRA-style) or ~0 (HESS-style)].
    double errMscw;
    double energy;    ///< Primary energy [TeV], assuming a gamma.
    double errEnergy;
    double xmax;      ///< Atmospheric depth of shower maximum [g/cm^2].
    double errXmax;

    public ShowerParameters() {
        imgList = new int[Constants.H_MAX_TEL];
    }

    public boolean readShower(EventIOBuffer buffer) {
        //TODO implement
        return false;
    }
}
