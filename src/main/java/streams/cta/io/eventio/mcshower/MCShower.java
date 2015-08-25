package streams.cta.io.eventio.mcshower;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.eventio.EventIOConstants;
import streams.cta.io.eventio.EventIOBuffer;
import streams.cta.io.eventio.EventIOHeader;

/**
 * Implementation of reading an MC Shower block from EventIO stream. For comparison consider
 * io_hess.{h,c} files from hessioxxx package. Created by alexey on 16.06.15.
 */
public class MCShower {

    static Logger log = LoggerFactory.getLogger(MCShower.class);

    long showerNum;

    /**
     * Particle ID of primary. Was in CORSIKA convention where detector_prog_vers in MC run header
     * was 0, and is now 0 (gamma), 1(e-), 2(mu-), 100*A+Z for nucleons and nuclei, negative for
     * antimatter.
     */
    int primaryId;

    /**
     * primary energy [TeV]
     */
    public float energy;

    /**
     * Azimuth (N->E) [rad]
     */
    float azimuth;

    /**
     * Altitude [rad]
     */
    float altitude;

    /**
     * Atmospheric depth where particle started [g/cm^2].
     */
    float depthStart;

    /**
     * height of first interaction a.s.l. [m]
     */
    float hFirstInt;

    /**
     * Atmospheric depth of shower maximum [g/cm^2], derived from all charged particles.
     */
    float xmax;

    /**
     * Height of shower maximum [m] in xmax.
     */
    float hmax;

    /**
     * Atm. depth of maximum in electron number.
     */
    float emax;

    /**
     * Atm. depth of max. in Cherenkov photon emission.
     */
    float cmax;

    /**
     * Number of profiles filled.
     */
    int numProfiles;

    ShowerProfile[] profiles;
    ShowerExtraParameters extraParameters;

    public boolean readMCShower(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 2) {
                    log.error("Unsupported MC shower version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                showerNum = header.getIdentification();

                primaryId = buffer.readInt32();
                energy = buffer.readFloat();
                azimuth = buffer.readFloat();
                altitude = buffer.readFloat();
                if (header.getVersion() >= 1) {
                    depthStart = buffer.readFloat();
                }
                hFirstInt = buffer.readFloat();
                xmax = buffer.readFloat();

                if (header.getVersion() >= 1) {
                    hmax = buffer.readFloat();
                    emax = buffer.readFloat();
                    cmax = buffer.readFloat();
                } else {
                    hmax = 0f;
                    emax = 0f;
                    cmax = 0f;
                }

                numProfiles = buffer.readInt16();

                int minProfiles = numProfiles < EventIOConstants.H_MAX_PROFILE ?
                        numProfiles : EventIOConstants.H_MAX_PROFILE;
                profiles = new ShowerProfile[minProfiles];

                // fill the ShowerProfiles
                for (int i = 0; i < minProfiles; i++) {
                    ShowerProfile profile = new ShowerProfile();
                    if (!profile.readShowerProfile(buffer)){
                        log.error("Error reading shower profile.");
                    }
                    profiles[i] = profile;
                }

                extraParameters = new ShowerExtraParameters();
                if (header.getVersion() >= 2) {
                    if (!extraParameters.readShowerExtraParameters(buffer)) {
                        header.getItemEnd();
                        log.error("Something went wrong while reading shower extra parameters");
                        return false;
                    }
                } else {
                    extraParameters.clearShowerExtraParameters();
                }
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}