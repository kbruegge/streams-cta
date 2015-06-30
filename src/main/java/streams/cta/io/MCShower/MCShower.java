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
    int primaryId;      ///< Particle ID of primary. Was in CORSIKA convention
    ///< where detector_prog_vers in MC run header was 0,
    ///< and is now 0 (gamma), 1(e-), 2(mu-), 100*A+Z
    ///< for nucleons and nuclei, negative for antimatter.
    double energy;       ///< primary energy [TeV]
    double azimuth;      ///< Azimuth (N->E) [rad]
    double altitude;     ///< Altitude [rad]
    double depthStart;  ///< Atmospheric depth where particle started [g/cm^2].
    double hFirstInt;  ///< height of first interaction a.s.l. [m]
    double xmax;         ///< Atmospheric depth of shower maximum [g/cm^2],
    ///< derived from all charged particles.
    double hmax;         ///< Height of shower maximum [m] in xmax.
    double emax;         ///< Atm. depth of maximum in electron number.
    double cmax;         ///< Atm. depth of max. in Cherenkov photon emission.
    int numProfiles;    ///< Number of profiles filled.
    ShowerProfile[] profile;
    ShowerExtraParameters extraParameters;


    public MCShower() {
        profile = new ShowerProfile[Constants.H_MAX_PROFILE];
        extraParameters = new ShowerExtraParameters();
    }

    public static MCShower readMCShower(EventIOBuffer buffer, EventIOHeader header)
            throws IOException {
        if (header.getVersion() > 2) {
            log.error("Unsupported MC shower version: " + header.getVersion());
            buffer.skipBytes(header.getLength());
            return null;
        }

        MCShower mcShower = new MCShower();
        mcShower.showerNum = header.getIdentification();

        mcShower.primaryId = buffer.readInt32(); // int32
        mcShower.energy = buffer.readReal(); // real
        mcShower.azimuth = buffer.readReal(); // real
        mcShower.altitude = buffer.readReal(); // real
        if (header.getVersion() >= 1) {
            mcShower.depthStart = buffer.readReal(); // real
        }
        mcShower.hFirstInt = buffer.readReal();
        mcShower.xmax = buffer.readReal();
        mcShower.hmax = mcShower.emax = mcShower.cmax = 0d;

        if (header.getVersion() >= 1) {
            mcShower.hmax = buffer.readReal();
            mcShower.emax = buffer.readReal();
            mcShower.cmax = buffer.readReal();
        }

        mcShower.numProfiles = buffer.readInt16(); // short

        // fill the ShowerProfiles
        for (int i = 0; i < mcShower.numProfiles && i < Constants.H_MAX_PROFILE; i++) {
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
            mcShower.profile[i] = profile;
        }

        if (header.getVersion() >= 2) {
            mcShower.extraParameters = readShowerExtraParameters(buffer);
            if (mcShower.extraParameters == null) {
                log.error("Something went wrong while reading shower extra parameters");
                // TODO: can something go wrong? possibly skip until the next block?!
            }
        } else {
            mcShower.extraParameters = clearShowerExtraParameters(mcShower.extraParameters);
        }
        return mcShower;
    }

    /**
     * Reset all the values for the object of type ShowerExtraParameters
     *
     * @param extraParameters object with the default values set
     */
    private static ShowerExtraParameters clearShowerExtraParameters(
            ShowerExtraParameters extraParameters) {
        extraParameters.id = 0;
        extraParameters.isSet = 0;
        extraParameters.weight = 1.0;

        if (extraParameters.iparam != null) {
            for (int i = 0; i < extraParameters.niparam; i++) {
                extraParameters.iparam[i] = 0;
            }
        }

        if (extraParameters.fparam != null) {
            for (int i = 0; i < extraParameters.nfparam; i++) {
                extraParameters.fparam[i] = 0;
            }
        }

        return extraParameters;
    }

    private static ShowerExtraParameters readShowerExtraParameters(EventIOBuffer buffer)
            throws IOException {

        // TODO: check this implementation after such extra parameters has been found
        log.error("Please check the implementation of readShowerExtraParameters " +
                "method and then remove this output.");

        // read the header of extra parameters
        EventIOHeader headerExtraParameters = new EventIOHeader(buffer);

        if (headerExtraParameters.findAndReadNextHeader()) {
            ShowerExtraParameters ep = new ShowerExtraParameters();
            ep.isSet = 0;

            if (headerExtraParameters.getVersion() != 1) {
                buffer.skipBytes(headerExtraParameters.getLength());
                log.error("Skipping MCShower because version is not 1, but "
                        + headerExtraParameters.getVersion());
            }

            ep.id = headerExtraParameters.getIdentification();
            ep.weight = buffer.readReal();

            // detect number of integer and float parameters dynamically
            long ni = buffer.readCount();
            long nf = buffer.readCount();

            // fill the iparam list
            if (ni > 0) {
                if (ni != ep.niparam) {
                    ep.iparam = new int[(int) ni];
                    for (int i = 0; i < ni; i++) {
                        ep.iparam[i] = buffer.readInt32();
                    }
                }
            }
            ep.niparam = ni;

            // fill the fparam list
            if (nf > 0) {
                if (nf != ep.nfparam) {
                    ep.fparam = new double[(int) nf];
                    for (int i = 0; i < nf; i++) {
                        ep.fparam[i] = buffer.readReal();
                    }
                }
            }
            ep.nfparam = nf;

            ep.isSet = 1;

            // count the levels down etc.
            headerExtraParameters.getItemEnd();

            return ep;
        } else {
            log.error("Something went wrong while searching for or reading "
                    + "header for the subobject 'shower extra parameters'.");
            return null;
        }
    }
}