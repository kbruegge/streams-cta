package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;

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
    double h_first_int;  ///< height of first interaction a.s.l. [m]
    double xmax;         ///< Atmospheric depth of shower maximum [g/cm^2],
    ///< derived from all charged particles.
    double hmax;         ///< Height of shower maximum [m] in xmax.
    double emax;         ///< Atm. depth of maximum in electron number.
    double cmax;         ///< Atm. depth of max. in Cherenkov photon emission.
    int numProfiles;    ///< Number of profiles filled.
    ShowerProfile[] profile;
    ShowerExtraParameters extra_parameters;


    public MCShower() {
        profile = new ShowerProfile[Constants.H_MAX_PROFILE];
        extra_parameters = new ShowerExtraParameters();
    }

    public static MCShower readMCShower(EventIOBuffer buffer, EventIOHeader header)
            throws IOException {
        if (header.version > 2) {
            log.error("Unsupported MC shower version: " + header.version);
            buffer.dataStream.skipBytes(header.length);
            return null;
        }

        MCShower mcShower = new MCShower();
        mcShower.showerNum = header.identification;

        mcShower.primaryId = buffer.readInt32(); // int32
        mcShower.energy = buffer.readReal(); // real
        mcShower.azimuth = buffer.readReal(); // real
        mcShower.altitude = buffer.readReal(); // real
        if (header.version >= 1) {
            mcShower.depthStart = buffer.readReal(); // real
        }
        mcShower.h_first_int = buffer.readReal();
        mcShower.xmax = buffer.readReal();
        mcShower.hmax = mcShower.emax = mcShower.cmax = 0d;

        if (header.version >= 1) {
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

        if (header.version >= 2) {
            mcShower.extra_parameters = readShowerExtraParameters(buffer);
            if (mcShower.extra_parameters == null){
                log.error("Something went wrong while reading shower extra parameters");
                // TODO: can something go wrong? possibly skip until the next block?!
            }
        } else {
            clearShowerExtraParameters(mcShower.extra_parameters);
        }
        return mcShower;
    }

    private static void clearShowerExtraParameters(ShowerExtraParameters extra_parameters) {
        //TODO: implement
        extra_parameters.id = 0;
        extra_parameters.isSet = 0;
        extra_parameters.weight = 1.0;

//        if ( ep->iparam != NULL )
//        {
//            for (i=0; i<ep->niparam; i++)
//                ep->iparam[i] = 0;
//        }
//        if ( ep->fparam != NULL )
//        {
//            for (i=0; i<ep->nfparam; i++)
//                ep->fparam[i] = 0;
//        }
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

            if (headerExtraParameters.version != 1) {
                buffer.dataStream.skipBytes(headerExtraParameters.length);
                log.error("Skipping MCShower because version is not 1, but " + headerExtraParameters.version);
            }

            ep.id = headerExtraParameters.identification;
            ep.weight = buffer.readReal();

            // detect number of integer and float parameters dynamically
            long ni = buffer.readCount();
            long nf = buffer.readCount();

            // fill the iparam list
            if (ni > 0){
                if (ni != ep.niparam){
                    ep.iparam = new int[(int) ni];
                    for (int i = 0; i < ni; i++) {
                        ep.iparam[i] = buffer.readInt32();
                    }
                }
            }
            ep.niparam = ni;

            // fill the fparam list
            if (nf > 0){
                if (nf != ep.nfparam){
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
            return null;
        }
    }
}


class ShowerExtraParameters {
    /**
     * May identify to the user what the parameters should mean.
     */
    long id;

    /**
     * May be reset after writing the parameter block and must thus be set to 1 for each shower for
     * which the extra parameters should get recorded.
     */
    int isSet;

    /**
     * To be used if the weight of a shower may change during processing, e.g. when shower
     * processing can be aborted depending on how quickly the electromagnetic component builds up
     * and the remaining showers may have a larger weight to compensate for that. For backwards
     * compatibility this should be set to 1.0 when no additional weight is needed.
     */
    double weight;

    //TODO: some further parameters need to be implemented
    //size_t niparam;/**< Number of extra integer parameters. */
    //int *iparam;   /**< Space for extra integer parameters, at least of size
    // niparam. */
    //size_t nfparam;/**< Number of extra floating-point parameters. */
    //float *fparam;   /**< Space for extra floats, at least of size nfparam. */
}


class ShowerProfile {
    /**
     * @short Type of profile (also determines units below).
     *
     * Temptative definitions:
     * @li 1000*k + 1:  Profile of all charged particles.
     * @li 1000*k + 2:  Profile of electrons+positrons.
     * @li 1000*k + 3:  Profile of muons.
     * @li 1000*k + 4:  Profile of hadrons.
     * @li 1000*k + 10: Profile of Cherenkov photon emission [1/m].
     *
     * The value of k specifies the binning:
     * @li k = 0: The profile is in terms of atmospheric depth along the shower axis.
     * @li k = 1: in terms of vertical atmospheric depth.
     * @li k = 2: in terms of altitude [m] above sea level.
     */
    int id;
    int numSteps;       ///< Number of histogram steps
    int maxSteps;       ///< Number of allowed steps as allocated for content
    double start;        ///< Start of ordinate ([m] or [g/cm^2])
    double end;          ///< End of it.
    double binsize;      ///< (End-Start)/numSteps; not saved
    double[] content;     ///< Histogram contents (allocated on demand).
}