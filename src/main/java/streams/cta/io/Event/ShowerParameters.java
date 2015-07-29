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

    /**
     * Azimuth angle [radians from N->E]
     */
    double azimuthAngle;
    double altitude;       ///< Altitude [radians]
    double errDirX;  ///< Error estimate in nominal plane X direction (|| Alt) [rad]
    double errDirY;  ///< Error estimate in nominal plane Y direction (|| Az) [rad]
    double errDir3;  ///< ?
    double corePosX;        ///< X core position [m]
    double corePosY;        ///< Y core position [m]
    double errCoreX; ///< Error estimate in X coordinate [m]
    double errCoreY; ///< Error estimate in Y coordinate [m]
    double errCore3; ///< ?
    double meanScaledLength;      ///< Mean scaled image length [gammas ~1 (HEGRA-style) or ~0 (HESS-style)].
    double errMeanScaledLength;
    double meanScaledWidth;      ///< Mean scaled image width [gammas ~1 (HEGRA-style) or ~0 (HESS-style)].
    double errMeanScaledWidth;
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
                    errDirX = buffer.readReal();
                    errDirY = buffer.readReal();
                    errDir3 = buffer.readReal();
                } else {
                    errDirX = 0.;
                    errDirY = 0.;
                    errDir3 = 0.;
                }

                if ((resultBits & 0x04) != 0) {
                    corePosX = buffer.readReal();
                    corePosY = buffer.readReal();
                } else {
                    corePosX = 0.;
                    corePosY = 0.;
                }

                if ((resultBits & 0x08) != 0) {
                    errCoreX = buffer.readReal();
                    errCoreY = buffer.readReal();
                    errCore3 = buffer.readReal();
                } else {
                    errCoreX = 0.;
                    errCoreY = 0.;
                    errCore3 = 0.;
                }

                if ((resultBits & 0x10) != 0) {
                    meanScaledLength = buffer.readReal();
                    meanScaledWidth = buffer.readReal();
                } else {
                    meanScaledLength = -1;
                    meanScaledWidth = -1;
                }

                if ((resultBits & 0x20) != 0) {
                    errMeanScaledLength = buffer.readReal();
                    errMeanScaledWidth = buffer.readReal();
                } else {
                    errMeanScaledLength = errMeanScaledWidth = 0.;
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

                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
