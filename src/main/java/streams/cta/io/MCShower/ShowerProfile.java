package streams.cta.io.MCShower;

/**
 * Created by alexey on 30.06.15.
 */
public class ShowerProfile {
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