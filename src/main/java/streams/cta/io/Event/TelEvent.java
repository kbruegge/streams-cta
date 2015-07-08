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
    int knownTimeTrgsect;

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
    AdcData[] raw;

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

//    int numPhysAddr;      ///< (not used)
//    int physAddr[4*H_MAX_DRAWERS];///< (not used)

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
    }

    public boolean readTelEvent(EventIOBuffer buffer) {
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
                if (raw != null){
                    known = false;
                }
                if (pixtm != null){
                    pixtm.known = false;
                }

                // preinitialize ImgData array
                ImgData [] img = this.img;
                if (this.img != null) {
                    for (int j = 0; j < numImageSets; j++) {
                        img[j].known = false;
                    }
                }

                // read telescope specific event header
                if(!readTelEventHeader(buffer)){
                    log.error("Error reading telescope event header.");
                    header.getItemEnd();
                    return false;
                }

                // pixel lists only available since version 1
                triggerPixels.pixels = 0;
                imagePixels.pixels = 0;

                boolean running = false;
                //TODO when do we stop reading?!
                while(running){
                    int type = buffer.nextSubitemType();
                    switch (type){
                        case Constants.TYPE_TELADCSUM:
                            //TODO implement reading teladc_sums and WHAT parameter
                            break;
                        case Constants.TYPE_TELADCSAMP:
                            //TODO implement reading teladc_samples and WHAT parameter
                            break;
                        case Constants.TYPE_PIXELTIMING:
                            //TODO implement reading pixtime and WHAT parameter
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
                            if (type > 0){
                                log.error("Skipping telescope event sub-item of type " + type
                                        + " for telescope " + this.telId);
                                //TODO skip subitem
                            }else{
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

    private boolean readTelEventHeader(EventIOBuffer buffer) {
        //TODO implement
        return false;
    }
}