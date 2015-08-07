package streams.cta.io.runheader;

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
    float[] direction;

    /**
     * Offset of pointing dir. in camera f.o.v. divided by focal length, i.e. converted to
     * [radians]: [0]=Camera x (downwards in normal pointing, i.e. increasing Alt, [1]=Camera y ->
     * Az).
     */
    float[] offsetFov;

    /**
     * Atmospheric depth of convergence point. In [g/cm^2] from the top of the atmosphere along the
     * system viewing direction. Typically 0 for parallel viewing or about Xmax(0.x TeV) for
     * convergent viewing.
     */
    float convDepth;

    /**
     * Reference position for convergent pointing. X,y in [m] at the telescope reference height.
     */
    float[] convRefPos;

    /**
     * Number of telescopes involved.
     */
    public int numberTelescopes;

    /**
     * ID numbers of telescopes used in this run.
     */
    public short[] telId;

    /**
     * x,y,z positions of the telescopes [m]. x is counted from array reference position towards
     * North, y towards West, z upwards.
     */
    float[][] telPos;

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
                convRefPos = new float[2];

                direction = buffer.readVectorOfFloats(2);
                offsetFov = buffer.readVectorOfFloats(2);
                convDepth = buffer.readFloat();
                if (header.getVersion() >= 1) {
                    // New in version 1
                    convRefPos = buffer.readVectorOfFloats(2);
                } else {
                    convRefPos[0] = 0.f;
                    convRefPos[1] = 0.f;
                }
                numberTelescopes = buffer.readInt32();
                telId = buffer.readVectorOfShorts(numberTelescopes);

                buffer.setTelIdx(numberTelescopes, telId);

                //TODO check if is the right conversion from C to JAVA!
                //get_vector_of_real(&tel_pos[0][0], 3 * numberTelescopes, iobuf);
                telPos = new float[3][numberTelescopes];
                for (int i = 0; i < 3; i++) {
                    telPos[i] = buffer.readVectorOfFloats(numberTelescopes);
                }
                minTelTrig = buffer.readInt32();
                duration = buffer.readInt32();

                //TODO check if it is the right conversion from C to JAVA!
                //get_string(line, sizeof(line) - 1, iobuf);
                target = buffer.readString(1024 - 1);

                observer = buffer.readString(1024 - 1);

                return header.getItemEnd();
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }
}
