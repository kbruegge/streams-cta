package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;
import streams.cta.io.HTime;

/**
 * Created by alexey on 30.06.15.
 */
public /**
 * Central trigger event data
 */
class CentralEvent {

    static Logger log = LoggerFactory.getLogger(CentralEvent.class);

    /**
     * Global event count.
     */
    int globCount;

    /**
     * CPU time at central trigger station.
     */
    HTime cpuTime;

    /**
     * GPS time at central trigger station.
     */
    HTime gpsTime;

    /**
     * Bit pattern of telescopes having sent a trigger signal to the central station. (Historical;
     * only useful for small no. of telescopes.)
     */
    int teltrgPattern;

    /**
     * Bit pattern of telescopes having sent event data that could be merged. (Historical; only
     * useful for small no. of telescopes.)
     */
    int teldataPattern;

    /**
     * How many telescopes triggered.
     */
    int numTelTriggered;

    /**
     * List of IDs of triggered telescopes.
     */
    int[] teltrgList;

    /**
     * Relative time of trigger signal after correction for nominal delay [ns].
     */
    float[] teltrgTime;

    /**
     * Bit mask which type of trigger fired.
     */
    int[] teltrgTypeMask;

    /**
     * Time of trigger separate for each type.
     */
    float[][] teltrgTimeByType;

    /**
     * Number of telescopes expected to have data.
     */
    int numTelData;

    /**
     * List of IDs of telescopes with data.
     */
    int[] teldataList;

    public CentralEvent() {
        teltrgList = new int[Constants.H_MAX_TEL];
        teltrgTime = new float[Constants.H_MAX_TEL];
        teltrgTypeMask = new int[Constants.H_MAX_TEL];
        teltrgTimeByType = new float[Constants.H_MAX_TEL][Constants.MAX_TEL_TRIGGERS];
        teldataList = new int[Constants.H_MAX_TEL];

        globCount = 0;
        teltrgPattern = 0;
        teldataPattern = 0;
        numTelTriggered = 0;
        numTelData = 0;
    }

    public void readCentralEvent(EventIOBuffer buffer){

        EventIOHeader headerNext = new EventIOHeader(buffer);
        try {
            if (headerNext.findAndReadNextHeader()) {
                if (headerNext.getVersion() > 2){
                    log.error("Unsupported central event version: " + headerNext.getVersion());
                    headerNext.getItemEnd();
                }else{
                    globCount = (int) headerNext.getIdentification();
                    cpuTime.readTime(buffer);
                    gpsTime.readTime(buffer);
                    teltrgPattern = buffer.readInt32();
                    teldataPattern = buffer.readInt32();

                    if (headerNext.getVersion() >= 1){
                        numTelTriggered = buffer.readShort();

                        if (numTelTriggered > Constants.H_MAX_TEL){
                            log.error("Invalid number of triggered telescopes " + numTelTriggered
                                    + " in central trigger block for event " + globCount);
                            numTelTriggered = 0;
                            headerNext.getItemEnd();
                        }

                        teltrgList = buffer.readVectorOfInts(numTelTriggered);
                        teltrgTime = buffer.readVectorOfFloats(numTelTriggered);
                        numTelData = buffer.readShort();

                        if (numTelData > Constants.H_MAX_TEL){
                            log.error("Invalid number of telescopes with data " + numTelData
                                    + " in central trigger block for event " + globCount);
                            numTelTriggered = 0;
                            headerNext.getItemEnd();
                        }

                        teldataList = buffer.readVectorOfInts(numTelData);
                    } else{
                        numTelTriggered = 0;
                        numTelData = 0;
                    }

                    //TODO wtf? versions greater than 2 are not supported so just check for ==2?
                    if (headerNext.getVersion() >= 2){
                        for (int i = 0; i < numTelTriggered; i++) {
                            //TODO first check for reading count!!! add different versions for 16, 32, 64
                            teltrgTypeMask[i] = (int) buffer.readCount();
                        }
                        for (int telCount = 0; telCount < numTelTriggered; telCount++) {
                            int ntt = 0;
                            for (int triggers = 0; triggers < Constants.MAX_TEL_TRIGGERS; triggers++) {
                                if ((teltrgTypeMask[telCount] & (1<<triggers)) == 1){
                                    ntt++;
                                    teltrgTimeByType[telCount][triggers] = teltrgTime[telCount];
                                }else{
                                    teltrgTimeByType[telCount][triggers] = 9999;
                                }
                            }

                            if (ntt > 1){
                                for (int triggers = 0; triggers < Constants.MAX_TEL_TRIGGERS; triggers++) {
                                    if ((teltrgTypeMask[telCount] & (1<< triggers)) == 1){
                                        teltrgTimeByType[telCount][triggers] = (float) buffer.readReal();
                                    }
                                }
                            }
                        }
                    } else {
                        for (int telCount = 0; telCount < numTelTriggered; telCount++) {
                            // older data was always majority trigger
                            teltrgTypeMask[telCount] = 1;
                            teltrgTimeByType[telCount][0] = teltrgTime[telCount];
                            teltrgTimeByType[telCount][1] = 9999;
                            teltrgTimeByType[telCount][2] = 9999;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
    }
}
