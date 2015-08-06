package streams.cta.io.mcshower;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;

/**
 * Type of profile (also determines units below).
 *
 * Temptative definitions:
 *
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
public class ShowerProfile {
    int id;

    /**
     * Number of histogram steps
     */
    int numSteps;

    /**
     * Number of allowed steps as allocated for content
     */
    int maxSteps;

    /**
     * Start of ordinate ([m] or [g/cm^2])
     */
    double start;

    /**
     * End of it.
     */
    double end;

    /**
     * (End-Start)/numSteps; not saved
     */
    double binSize;

    /**
     * Histogram contents (allocated on demand).
     */
    float[] content;

    /**
     * Read shower profile data. This data has no special header and is called from MCShower
     * reader.
     *
     * @param buffer EventIOBuffer to read from data stream
     * @return true if reading was successful, false otherwise.
     */
    public boolean readShowerProfile(EventIOBuffer buffer) {
        try {
            int skip = 0;
            id = buffer.readInt32();
            numSteps = buffer.readInt32();
            if (numSteps > maxSteps) {
                if (content != null) {
                    if (maxSteps > 0) {
                        content = null;
                    } else {
                        skip = 1;
                    }
                }
            }

            start = buffer.readReal();
            end = buffer.readReal();

            if (numSteps > 0) {
                binSize = (end - start) / (double) numSteps;
            }
            if (content == null) {
                content = new float[numSteps];
                maxSteps = numSteps;
            }

            if (skip == 1) {
                for (int j = 0; j < numSteps; j++) {
                    buffer.readReal();
                }
                numSteps *= -1;
            } else {
                content = buffer.readVectorOfFloats(numSteps);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}