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
    public int telId;

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
    public int maxImageSets;

    /**
     * Pointer to raw data, if any.
     */
    public AdcData raw;

    /**
     * Optional pixel (pulse shape) timing.
     */
    public PixelTiming pixtm;

    /**
     * Pointer to second moments, if any.
     */
    public ImgData[] img;

    /**
     * Pointer to calibrated pixel intensities, if available.
     */
    public PixelCalibrated pixcal;

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
        triggerPixels = new PixelList();
        imagePixels = new PixelList();
        cpuTime = new HTime();
        gpsTime = new HTime();
    }

    private void initSectorArrays(int numberTriggeredSectors){
        listTrgsect = new int[numberTriggeredSectors];
        timeTrgsect = new double[numberTriggeredSectors];
    }

    private void initPhysicalAdressArray(int numberAdresses){
        physAddr = new int[numberAdresses];
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
                if (img != null) {
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
                int wPixcal = 0;
                int telImg = 0;
                boolean readingSuccessful = true;
                while (readingSuccessful) {
                    int type = buffer.nextSubitemType();
                    switch (type) {
                        case Constants.TYPE_TELADCSUM:
                            if ((what & (Constants.RAWDATA_FLAG
                                    | Constants.RAWSUM_FLAG)) == 0 || raw == null) {
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
                                    log.warn("Telescope raw data ADC samples " +
                                            "not selected to be read.");
                                }
                                readingSuccessful = buffer.skipSubitem();
                                continue;
                            }

                            // preceded by sum data?
                            if (raw.known != 0) {
                                // sum + samples (perhaps different zero suppression)
                                readoutMode = 2;
                            } else {
                                //TODO do we need this? (good question by Bernloehr)
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
                            if (pixtm == null || (what & Constants.TIME_FLAG) == 0) {
                                if (wPixtm++ < 1) {
                                    log.warn("Telescope pixel timing data not selected to be read.");
                                }
                                readingSuccessful = buffer.skipSubitem();
                                continue;
                            }
                            readingSuccessful = pixtm.readPixTime(buffer);
                            break;
                        case Constants.TYPE_PIXELCALIB:
                            if (pixcal == null) {
                                if (wPixcal++ < 1) {
                                    log.warn("Telescope calibrated pixel intensities found, " +
                                            "allocating structures.");
                                }
                                pixcal = new PixelCalibrated();
                                //TODO in original we construct it with a sizeof(PixelCalibrated) and check whether it failed due to not enough memory
                                pixcal.telId = telId;
                            }
                            readingSuccessful = pixcal.readPixelCalibrated(buffer);
                            break;
                        case Constants.TYPE_TELIMAGE:
                            if (img == null || (what & Constants.IMAGE_FLAG) == 0) {
                                break;
                            }
                            if (telImg >= maxImageSets) {
                                log.warn("Not enough space to read all image sets.");
                                break;
                            }
                            readingSuccessful = img[telImg].readTelImage(buffer);
                            if (readingSuccessful) {
                                img[telImg].known = true;
                                telImg++;
                            }
                            numImageSets = telImg;
                            break;
                        case Constants.TYPE_PIXELLIST:
                            long id = buffer.nextSubitemIdent();
                            long code = id / 1000000;
                            long tid = id % 1000000;
                            if (code == 0 && tid == this.telId) {
                                readingSuccessful = triggerPixels.readPixelList(buffer);
                            } else if (code == 1 && tid == this.telId) {
                                readingSuccessful = imagePixels.readPixelList(buffer);
                                //TODO Bernloehr: Fix for missing number of pixels in image of older data format: */
                                if (img != null && img[0].known && img[0].pixels == 0) {
                                    img[0].pixels = imagePixels.pixels;
                                }
                            } else {
                                log.error("Skipping pixel list of type " + code
                                        + "for telescope " + tid);
                                readingSuccessful = buffer.skipSubitem();
                            }

                            break;
                        default:
                            if (type > 0) {
//                                log.info("Skipping telescope event sub-item of type " + type
//                                        + " for telescope " + this.telId);
                                readingSuccessful = buffer.skipSubitem();
                            } else {
                                header.getItemEnd();
                                return false;
                            }
                    }

                    // if reading was not successful, get to the end of this item
                    // and stop while loop
                    if (!readingSuccessful) {
                        // TODO this is not necessary as we do this later befor "return true"
                        //header.getItemEnd();
                        break;
                    }

                    this.known = true;
                }

                // TODO return the value from getItemEnd
                header.getItemEnd();
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
                            buffer.readShort() : buffer.readSCount32();

                    // initialize arrays with the right size
                    initSectorArrays(numListTrgsect);
                    for (int i = 0; i < numListTrgsect; i++) {
                        listTrgsect[i] = header.getVersion() <= 1 ?
                                buffer.readShort() : buffer.readSCount32();
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
                    numPhysAddr = headerGT1 ? buffer.readShort() : buffer.readSCount32();

                    // initialize array for physical addresses
                    initPhysicalAdressArray(numPhysAddr);

                    for (int i = 0; i < numPhysAddr; i++) {
                        physAddr[i] = headerGT1 ? buffer.readShort() : buffer.readSCount32();
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