package streams.cta.io.RunHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;

/**
 * Run header common to measured and simulated data.
 *
 * @author alexey
 */
public class RunHeader {

    static Logger log = LoggerFactory.getLogger(RunHeader.class);

    /** Recorded data: */

    /**
     * Run number.
     */
    int run;

    /**
     * Time of run start [UTC sec since 1970.0].
     */
    long time;

    /**
     * Data/pedestal/laser/muon run or MC run: MC run: -1, Data run: 1, Pedestal run: 2, Laser run:
     * 3, Muon run: 4.
     */
    int runType;

    /**
     * Tracking/pointing mode: 0: Az/Alt, 1: R.A./Dec. 2000
     */
    int trackingMode;

    /**
     * Normal or reverse tracking: 0: Normal, 1: reverse.
     */
    int reverseFlag;

    /**
     * Tracking/pointing direction in [radians]: [0]=Azimuth, [1]=Altitude in mode 0, [0]=R.A.,
     * [1]=Declination in mode 1.
     */
    double[] direction;

    /**
     * Offset of pointing dir. in camera f.o.v. divided by focal length, i.e. converted to
     * [radians]: [0]=Camera x (downwards in normal pointing, i.e. increasing Alt, [1]=Camera y ->
     * Az).
     */
    double[] offsetFov;

    /**
     * Atmospheric depth of convergence point. In [g/cm^2] from the top of the atmosphere along the
     * system viewing direction. Typically 0 for parallel viewing or about Xmax(0.x TeV) for
     * convergent viewing.
     */
    double convDepth;

    /**
     * Reference position for convergent pointing. X,y in [m] at the telescope reference height.
     */
    double[] convRefPos;

    /**
     * Number of telescopes involved.
     */
    public int ntel;

    /**
     * ID numbers of telescopes used in this run.
     */
    public int[] telId;

    /**
     * x,y,z positions of the telescopes [m]. x is counted from array reference position towards
     * North, y towards West, z upwards.
     */
    double[][] telPos;

    /**
     * Minimum number of tel. in system trigger.
     */
    int minTelTrig;

    /**
     * Nominal duration of run [s].
     */
    int duration;

    /**
     * Primary target object name.
     */
    String target;

    /**
     * Observer(s) starting or supervising run.
     */
    String observer;

    /**
     * For internal data handling only:
     */
    int maxLenTarget;
    int maxLenObserver;

    public boolean readRunHeader(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 2) {
                    log.error("Unsupported run header version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }

                run = buffer.readInt32();
                time = buffer.readLong();
                runType = buffer.readInt32();
                trackingMode = buffer.readInt32();
                if (header.getVersion() >= 2) {
                    // New in version 2!
                    reverseFlag = buffer.readInt32();
                } else {
                    reverseFlag = 0;
                }

                // init array (both others are getting arrays of right size directly)
                convRefPos = new double[2];

                direction = buffer.readVectorOfReals(2);
                offsetFov = buffer.readVectorOfReals(2);
                convDepth = buffer.readReal();
                if (header.getVersion() >= 1) {
                    // New in version 1
                    convRefPos = buffer.readVectorOfReals(2);
                } else {
                    convRefPos[0] = 0.;
                    convRefPos[1] = 0.;
                }
                ntel = buffer.readInt32();
                telId = buffer.readVectorOfInts(ntel);

                buffer.setTelIdx(ntel, telId);

                //TODO check if is the right conversion from C to JAVA!
                //get_vector_of_real(&tel_pos[0][0], 3 * ntel, iobuf);
                telPos = new double[3][ntel];
                for (int i = 0; i < 3; i++) {
                    telPos[i] = buffer.readVectorOfReals(ntel);
                }
                minTelTrig = buffer.readInt32();
                duration = buffer.readInt32();

                //TODO check if it is the right conversion from C to JAVA!
                //get_string(line, sizeof(line) - 1, iobuf);
                char[] line = buffer.readString(1024 - 1);

                if (target != null && maxLenTarget > 0) {
                    target = String.valueOf(line, 0, maxLenTarget);
                } else {
                    target = String.valueOf(line);
                }

                line = buffer.readString(1024 - 1);

                if (observer != null && maxLenObserver > 0) {
                    observer = String.valueOf(line, 0, maxLenObserver);
                } else {
                    observer = String.valueOf(line);
                }
                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
