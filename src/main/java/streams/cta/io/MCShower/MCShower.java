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
    double energy;

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

    public MCShower() {
        extraParameters = new ShowerExtraParameters();
    }

    public boolean readMCShower(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 2) {
                    //TODO getItemEnd should be used?
                    log.error("Unsupported MC shower version: " + header.getVersion());
                    header.getItemEnd();
                    //buffer.skipBytes((int) header.getLength());
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
                    int skip = 0;
                    ShowerProfile profile = new ShowerProfile();
                    profile.id = buffer.readInt32();
                    profile.numSteps = buffer.readInt32();
                    if (profile.numSteps > profile.maxSteps) {
                        if (profile.content != null) {
                            if (profile.maxSteps > 0) {
                                profile.content = null;
                            } else {
                                skip = 1;
                            }
                        }
                    }

                    profile.start = buffer.readReal();
                    profile.end = buffer.readReal();

                    if (profile.numSteps > 0) {
                        profile.binsize = (profile.end - profile.start) / (double) profile.numSteps;
                    }
                    if (profile.content == null) {
                        profile.content = new double[profile.numSteps];

                        // TODO: consider check whether there is enough space for allocation
                        // here in original code there is a check
                        // whether content could have been allocated
                        // otherwise there were too little space

                        profile.maxSteps = profile.numSteps;
                    }

                    if (skip == 1) {
                        for (int j = 0; j < profile.numSteps; j++) {
                            buffer.readReal();
                        }
                        profile.numSteps *= -1;
                    } else {
                        profile.content = buffer.readVectorOfReals(profile.numSteps);
                    }
                    profiles[i] = profile;
                }

                if (header.getVersion() >= 2) {
                    boolean success = extraParameters.readShowerExtraParameters(buffer);
                    if (!success) {
                        log.error("Something went wrong while reading shower extra parameters");
                        // TODO: can something go wrong? possibly skip until the next block?!
                    }
                } else {
                    extraParameters.clearShowerExtraParameters();
                }
                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}