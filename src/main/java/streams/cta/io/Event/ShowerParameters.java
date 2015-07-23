package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Reconstructed shower parameters.
 *
 * @author alexey
 */
public class ShowerParameters {

    static Logger log = LoggerFactory.getLogger(ShowerParameters.class);

    public int known;
    public int numTrg;      ///< Number of telescopes contributing to central trigger.
    int numRead;     ///< Number of telescopes read out.
    int numImg;      ///< Number of images used for shower parameters.
    int imgPattern;  ///< Bit pattern of which telescopes were used (for small no. of telescopes only).
    int[] imgList; ///< With more than 16 or 32 telescopes, we can only use the list.

    /**
     * Bit pattern of what results are available: Bits 0 + 1: direction + errors Bits 2 + 3: core
     * position + errors Bits 4 + 5: mean scaled image shape + errors Bits 6 + 7: energy + error
     * Bits 8 + 9: shower maximum + error
     */
    long resultBits;

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

    public boolean readShower(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                known = 0;

                if (header.getVersion() > 2) {
                    log.error("Unsupported reconstructed shower version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                resultBits = header.getIdentification();

                numTrg = buffer.readShort();
                numRead = buffer.readShort();
                numImg = buffer.readShort();

                if (header.getVersion() >= 1) {
                    imgPattern = buffer.readInt32();
                } else {
                    imgPattern = 0;
                }

                if (header.getVersion() >= 2) {
                    imgList = buffer.readVectorOfInts(numImg);
                }

                if ((resultBits & 0x01) != 0) {
                    azimuthAngle = buffer.readReal();
                    altitude = buffer.readReal();
                } else {
                    azimuthAngle = 0.;
                    altitude = 0.;
                }

                if ((resultBits & 0x02) != 0) {
                    errDir1 = buffer.readReal();
                    errDir2 = buffer.readReal();
                    errDir3 = buffer.readReal();
                } else {
                    errDir1 = 0.;
                    errDir2 = 0.;
                    errDir3 = 0.;
                }

                if ((resultBits & 0x04) != 0) {
                    xc = buffer.readReal();
                    yc = buffer.readReal();
                } else {
                    xc = 0.;
                    yc = 0.;
                }

                if ((resultBits & 0x08) != 0) {
                    errCore1 = buffer.readReal();
                    errCore2 = buffer.readReal();
                    errCore3 = buffer.readReal();
                } else {
                    errCore1 = 0.;
                    errCore2 = 0.;
                    errCore3 = 0.;
                }

                if ((resultBits & 0x10) != 0) {
                    mscl = buffer.readReal();
                    mscw = buffer.readReal();
                } else {
                    mscl = -1;
                    mscw = -1;
                }

                if ((resultBits & 0x20) != 0) {
                    errMscl = buffer.readReal();
                    errMscw = buffer.readReal();
                } else {
                    errMscl = errMscw = 0.;
                }

                if ((resultBits & 0x40) != 0) {
                    energy = buffer.readReal();
                } else {
                    energy = -1.;
                }

                if ((resultBits & 0x80) != 0) {
                    errEnergy = buffer.readReal();
                } else {
                    errEnergy = 0.;
                }

                xmax = 0.;

                if ((resultBits & 0x0100) != 0) {
                    xmax = buffer.readReal();
                }

                errXmax = 0.;

                if ((resultBits & 0x0200) != 0) {
                    errXmax = buffer.readReal();
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
}
