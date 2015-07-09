package streams.cta.io.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;
import streams.cta.io.EventIOBuffer;
import streams.cta.io.EventIOHeader;
import streams.cta.io.HTime;

/**
 * Event raw and image data from one telescope. Created by alexey on 30.06.15.
 */
public class TelEvent {

    static Logger log = LoggerFactory.getLogger(TelEvent.class);

    boolean known;

    /**
     * The telescope ID number (1 ... n)
     */
    int telId;

    /**
     * The counter for local triggers.
     */
    int locCount;

    /**
     * The counter for system triggers.
     */
    long globCount;

    /**
     * Camera CPU system time of event.
     */
    HTime cpuTime;

    /**
     * GPS time of event, if any.
     */
    HTime gpsTime;

    /**
     * 1=internal (event data) or 2=external (calib data).
     */
    int trgSource;

    /**
     * Number of trigger groups (sectors) listed.
     */
    int numListTrgsect;

    /**
     * List of triggered groups (sectors).
     */
    int[] listTrgsect;

    /**
     * Are the trigger times known? (0/1)
     */
    boolean knownTimeTrgsect;

    /**
     * Times when trigger groups (as in list) fired.
     */
    double[] timeTrgsect;

//   char type_trgsect[H_MAX_SECTORS]; ///< 0: majority, 1: analog sum, 2: digital sum.

    /**
     * Sum mode (0) or sample mode (1 ... 255, normally: 1).
     */
    int readoutMode;

    /**
     * how many 'img' sets are available.
     */
    int numImageSets;

    /**
     * how many 'img' sets were allocated.
     */
    int maxImageSets;

    /**
     * Pointer to raw data, if any.
     */
    AdcData raw;

    /**
     * Optional pixel (pulse shape) timing.
     */
    PixelTiming pixtm;

    /**
     * Pointer to second moments, if any.
     */
    ImgData[] img;

    /**
     * Pointer to calibrated pixel intensities, if available.
     */
    PixelCalibrated[] pixcal;

    //TODO check whether the comment is right and these variables are not used (then just skip those parts in stream)
    int numPhysAddr;        ///< (not used)
    int[] physAddr;         ///< (not used)

    /**
     * List of triggered pixels.
     */
    PixelList triggerPixels;

    /**
     * Pixels included in (first) image.
     */
    PixelList imagePixels;

    public TelEvent() {
        listTrgsect = new int[Constants.H_MAX_SECTORS];
        timeTrgsect = new double[Constants.H_MAX_SECTORS];
        pixtm = new PixelTiming();
        //TODO init when the numImageSets known
//        raw = new AdcData();
//        img = new ImgData();
//        pixcal = new PixelCalibrated();
        triggerPixels = new PixelList();
        imagePixels = new PixelList();
        physAddr = new int[4 * Constants.H_MAX_DRAWERS];
    }

    /**
     * Read telescope event for which telescope event header is needed.
     *
     * @param buffer EventIOBuffer to read from stream
     * @return true, if reading was successful, false otherwise
     */
    public boolean readTelEvent(EventIOBuffer buffer, int what) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {

                // check for wrong telescope id and unsupported version
                int telId = (header.getType() - Constants.TYPE_TEL_EVENT) % 100 +
                        100 * ((header.getType() - Constants.TYPE_TEL_EVENT) / 1000);
                if (telId < 0 || telId != this.telId) {
                    log.warn("Not a telescope event block or one for the wrong telescope.");
                    header.getItemEnd();
                    return false;
                }
                if (header.getVersion() > 1) {
                    log.error("Unsupported telescope event version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }
                globCount = header.getIdentification();
                if (raw != null) {
                    known = false;
                }
                if (pixtm != null) {
                    pixtm.known = false;
                }

                // preinitialize ImgData array
                ImgData[] img = this.img;
                if (this.img != null) {
                    for (int j = 0; j < numImageSets; j++) {
                        img[j].known = false;
                    }
                }

                // read telescope specific event header
                if (!readTelEventHeader(buffer)) {
                    log.error("Error reading telescope event header.");
                    header.getItemEnd();
                    return false;
                }

                // pixel lists only available since version 1
                triggerPixels.pixels = 0;
                imagePixels.pixels = 0;

                int wSum = 0;
                int wSamples = 0;
                int wPixtm = 0;
                boolean readingSuccessful = true;
                boolean running = false;
                //TODO when do we stop reading?!
                while (running) {
                    int type = buffer.nextSubitemType();
                    switch (type) {
                        case Constants.TYPE_TELADCSUM:
                            if ((what & (Constants.RAWDATA_FLAG | Constants.RAWSUM_FLAG)) == 0 || raw == null) {
                                if (wSum++ < 1) {
                                    log.warn("Telescope raw data ADC sums not selected to be read.");
                                }
                                readingSuccessful = buffer.skipSubitem();
                                continue;
                            }
                            readingSuccessful = raw.readTelADCSums(buffer);
                            readoutMode = 0;
                            if (readingSuccessful) {
                                known = true;
                            }
                            raw.telId = this.telId;
                            break;
                        case Constants.TYPE_TELADCSAMP:
                            if ((what & Constants.RAWDATA_FLAG) == 0 || raw == null) {
                                if (wSamples++ < 1) {
                                    log.warn("Telescope raw data ADC samples not selected to be read.");
                                }
                                readingSuccessful = buffer.skipSubitem();
                                continue;
                            }

                            // preceded by sum data?
                            if (raw.known != 0) {
                                // sum + samples (perhaps different zero suppression)
                                readoutMode = 2;
                            } else {
                                //TODO do we need this? (good question by Bernloehr
                                raw.resetAdc();
                                // adc samples, sums usually rebuild
                                readoutMode = 1;
                            }
                            readingSuccessful = raw.readTelACSSamples(buffer, what);
                            if (readingSuccessful) {
                                raw.known |= 2;
                            }

                            //TODO for ids beyound 31 bits maybe missing?! (Bernloehr)
                            raw.telId = this.telId;
                            break;
                        case Constants.TYPE_PIXELTIMING:
                            if (pixtm == null || (what & Constants.TIME_FLAG) == 0){
                                if (wPixtm++ < 1){
                                    log.warn("Telescope pixel timing data not selected to be read.");
                                }
                                readingSuccessful = buffer.skipSubitem();
                                continue;
                            }
                            readingSuccessful = pixtm.readPixTime(buffer);
                            break;
                        case Constants.TYPE_PIXELCALIB:
                            //TODO implement reading pixcalib and WHAT parameter
                            break;
                        case Constants.TYPE_TELIMAGE:
                            //TODO implement reading telimage and WHAT parameter
                            break;
                        case Constants.TYPE_PIXELLIST:
                            //TODO implement reading next subitem identification, pixel_list for different objects (triggerPixels and imagePixels)
                            break;
                        default:
                            if (type > 0) {
                                log.error("Skipping telescope event sub-item of type " + type
                                        + " for telescope " + this.telId);
                                //TODO skip subitem
                            } else {
                                header.getItemEnd();
                            }

                    }
                }

                return true;
            }
        } catch (IOException e) {
            log.error("Something went wrong while reading the header:\n" + e.getMessage());
        }
        return false;
    }

    /**
     * Read telescope event header that is needed to read telescope event correctly.
     *
     * @param buffer EventIOBuffer to read from stream
     * @return true if reading was successful, false otherwise
     */
    private boolean readTelEventHeader(EventIOBuffer buffer) {
        EventIOHeader header = new EventIOHeader(buffer);
        try {
            if (header.findAndReadNextHeader()) {
                if (header.getVersion() > 2) {
                    log.error("Unsupported telescope event header version: " + header.getVersion());
                    header.getItemEnd();
                    return false;
                }
                if (header.getIdentification() != telId) {
                    log.warn("Event header is for wrong telescope.");
                    header.getItemEnd();
                    return false;
                }

                locCount = buffer.readInt32();
                globCount = buffer.readInt32();
                cpuTime.readTime(buffer);
                gpsTime.readTime(buffer);

                //TODO what is t?
                int t = buffer.readShort();
                trgSource = t & 0xff;
                knownTimeTrgsect = false;

                if ((t & 0x100) != 0) {
                    numListTrgsect = header.getVersion() <= 1 ?
                            buffer.readShort() : buffer.readSCount();
                    for (int i = 0; i < numListTrgsect; i++) {
                        listTrgsect[i] = header.getVersion() <= 1 ?
                                buffer.readShort() : buffer.readSCount();
                    }
                    if (header.getVersion() <= 1 && (t & 0x400) != 0) {
                        for (int i = 0; i < numListTrgsect; i++) {
                            timeTrgsect[i] = buffer.readReal();
                        }
                        knownTimeTrgsect = true;
                    } else {
                        for (int i = 0; i < numListTrgsect; i++) {
                            timeTrgsect[i] = 0;
                        }
                    }
                }

                if ((t & 0x200) != 0) {
                    boolean headerGT1 = header.getVersion() <= 1;
                    numPhysAddr = headerGT1 ? buffer.readShort() : buffer.readSCount();
                    for (int i = 0; i < numPhysAddr; i++) {
                        physAddr[i] = headerGT1 ? buffer.readShort() : buffer.readSCount();
                    }
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