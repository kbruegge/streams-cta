package streams.cta.io.MCShower;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

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
    public double energy;

    /**
     * Azimuth (N->E) [rad]
     */
    double azimuth;

    /**
     * Altitude [rad]
     */
    double altitude;

    /**
     * Atmospheric depth where particle started [g/cm^2].
     */
    double depthStart;

    /**
     * height of first interaction a.s.l. [m]
     */
    double hFirstInt;

    /**
     * Atmospheric depth of shower maximum [g/cm^2], derived from all charged particles.
     */
    double xmax;

    /**
     * Height of shower maximum [m] in xmax.
     */
    double hmax;

    /**
     * Atm. depth of maximum in electron number.
     */
    double emax;

    /**
     * Atm. depth of max. in Cherenkov photon emission.
     */
    double cmax;

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
                energy = buffer.readReal();
                azimuth = buffer.readReal();
                altitude = buffer.readReal();
                if (header.getVersion() >= 1) {
                    depthStart = buffer.readReal();
                }
                hFirstInt = buffer.readReal();
                xmax = buffer.readReal();

                if (header.getVersion() >= 1) {
                    hmax = buffer.readReal();
                    emax = buffer.readReal();
                    cmax = buffer.readReal();
                } else {
                    hmax = 0d;
                    emax = 0d;
                    cmax = 0d;
                }

                numProfiles = buffer.readInt16();

                int minProfiles = numProfiles < Constants.H_MAX_PROFILE ?
                        numProfiles : Constants.H_MAX_PROFILE;
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