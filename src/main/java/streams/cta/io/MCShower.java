package streams.cta.io;

/**
 * Created by alexey on 16.06.15.
 */

public class MCShower {

    //TODO: move this to constants file
    int H_MAX_PROFILE = 10;

    long shower_num;
    int primary_id;      ///< Particle ID of primary. Was in CORSIKA convention
    ///< where detector_prog_vers in MC run header was 0,
    ///< and is now 0 (gamma), 1(e-), 2(mu-), 100*A+Z
    ///< for nucleons and nuclei, negative for antimatter.
    double energy;       ///< primary energy [TeV]
    double azimuth;      ///< Azimuth (N->E) [rad]
    double altitude;     ///< Altitude [rad]
    double depth_start;  ///< Atmospheric depth where particle started [g/cm^2].
    double h_first_int;  ///< height of first interaction a.s.l. [m]
    double xmax;         ///< Atmospheric depth of shower maximum [g/cm^2],
    ///< derived from all charged particles.
    double hmax;         ///< Height of shower maximum [m] in xmax.
    double emax;         ///< Atm. depth of maximum in electron number.
    double cmax;         ///< Atm. depth of max. in Cherenkov photon emission.
    int num_profiles;    ///< Number of profiles filled.
    ShowerProfile[] profile;
    ShowerExtraParameters extra_parameters;


    public MCShower() {
        profile = new ShowerProfile[H_MAX_PROFILE];
        extra_parameters = new ShowerExtraParameters();
    }
}


class ShowerExtraParameters {
    long id;       /**< May identify to the user what the parameters should mean. */
    int is_set;    /**< May be reset after writing the parameter block
     and must thus be set to 1 for each shower for
     which the extra parameters should get recorded. */
    double weight; /**< To be used if the weight of a shower may change during
     processing, e.g. when shower processing can be aborted
     depending on how quickly the electromagnetic component
     builds up and the remaining showers may have a larger
     weight to compensate for that.
     For backwards compatibility this should be set to 1.0
     when no additional weight is needed. */
    //size_t niparam;/**< Number of extra integer parameters. */
    //int *iparam;   /**< Space for extra integer parameters, at least of size
    // niparam. */
    //size_t nfparam;/**< Number of extra floating-point parameters. */
    //float *fparam;   /**< Space for extra floats, at least of size nfparam. */
}


class ShowerProfile {
    /**
     * @short    Type of profile (also determines units below).
     *
     *            Temptative definitions:
     *            @li 1000*k + 1:  Profile of all charged particles.
     *            @li 1000*k + 2:  Profile of electrons+positrons.
     *            @li 1000*k + 3:  Profile of muons.
     *            @li 1000*k + 4:  Profile of hadrons.
     *            @li 1000*k + 10: Profile of Cherenkov photon emission [1/m].
     *
     *            The value of k specifies the binning:
     *            @li k = 0: The profile is in terms of atmospheric depth
     *                along the shower axis.
     *            @li k = 1: in terms of vertical atmospheric depth.
     *            @li k = 2: in terms of altitude [m] above sea level.
     */
    int id;
    int num_steps;       ///< Number of histogram steps
    int max_steps;       ///< Number of allowed steps as allocated for content
    double start;        ///< Start of ordinate ([m] or [g/cm^2])
    double end;          ///< End of it.
    double binsize;      ///< (End-Start)/num_steps; not saved
    double[] content;     ///< Histogram contents (allocated on demand).
}