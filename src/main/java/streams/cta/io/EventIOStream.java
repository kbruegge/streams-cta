package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.cta.CTAEvent;
import streams.cta.Constants;
import streams.cta.io.Event.AdcData;
import streams.cta.io.Event.ImgData;
import streams.cta.io.Event.PixelTiming;
import streams.cta.io.Event.TelEvent;
import streams.cta.io.MCShower.MCShower;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    static boolean reverse = false;

    private DataInputStream dataStream;

    public static HashMap<Integer, String> eventioTypes;

    public EventIOData eventData;
    public EventIOBuffer buffer;

    @Parameter(
            required = false,
            description = "This value defines the size of the buffer of the BufferedInputStream",
            defaultValue = "8*1024")

    private int bufferSize = 8 * 1024;

    public EventIOStream(SourceURL url) {
        super(url);
    }

    public EventIOStream() {
        super();
    }

    @Override
    public void init() throws Exception {
        super.init();


        // try opening file from the given URL
        File f = new File(this.url.getFile());
        if (this.url.getProtocol().toLowerCase().startsWith("file")
                && !f.canRead()) {
            log.error("Cannot read file. Wrong path? " + f.getAbsolutePath());
            throw new FileNotFoundException("Cannot read file "
                    + f.getAbsolutePath());
        }

        // initialize data stream
        BufferedInputStream bStream = new BufferedInputStream(
                url.openStream(),
                bufferSize);
        dataStream = new DataInputStream(bStream);

        // initialize buffer containing the data stream to read from it
        buffer = new EventIOBuffer(dataStream);

        eventData = new EventIOData();

        // import the registered types
        importEventioRegisteredDatatypes();
    }

    @Override
    public Data readNext() throws Exception {

        Data item = null;
        EventIOHeader header = new EventIOHeader(buffer);
        if (header.findAndReadNextHeader(true)) {
            CTAEvent event;
            // MC Shower
            if (header.type == 2020) {
                header.findAndReadNextHeader();
                eventData.mcShower = MCShower.readMCShower(buffer, header);
                event = new CTAEvent(10, new byte[]{0, 1, 2});
                header.getItemEnd();
            } else if (header.type == Constants.TYPE_EVENT) {
                if (!eventData.event.readFullEvent(buffer, -1)) {
                    log.error("Error happened while reading full event data.");
                    //return null;
                }
                event = new CTAEvent(10, new byte[]{1, 2, 3,});
            } else if (header.type == 2000) {

                // Summary of a preceding run in the same file ?
//                if (!quiet && hsdata != NULL && hsdata->run_header.run > 0)
//                    show_run_summary(hsdata, nev, ntrg, plidx, wsum_all, wsum_trg,
//                            rmax_x, rmax_y, rmax_r);
//                else if (nev > 0)
//                    printf("%d of %d events triggered.\n", ntrg, nev);

                // Structures might be allocated from previous run
                if (eventData != null) {
                    // Free memory allocated inside ...
                    for (int itel = 0; itel < eventData.runHeader.ntel; itel++) {
                        if (eventData.event.teldata[itel].raw != null) {
                            eventData.event.teldata[itel].raw = null;
                        }
                        if (eventData.event.teldata[itel].pixtm != null) {
                            eventData.event.teldata[itel].pixtm = null;
                        }
                        if (eventData.event.teldata[itel].img != null) {
                            eventData.event.teldata[itel].img = null;
                        }
                        if (eventData.event.teldata[itel].pixcal != null) {
                            eventData.event.teldata[itel].pixcal = null;
                        }
                    }
                        /* Free main structure */
//                    if (!dst_processing) {
//                        free(hsdata);
//                        hsdata = NULL;
//                    }
                }

//                nev = ntrg = 0;
//                wsum_all = wsum_trg = 0.;

//                nrun++;

                if (!eventData.runHeader.readRunHeader(buffer)) {
                    log.error("Error happened while reading run header.");
                    return null;
                }

//                if (!quiet)
//                    printf("Reading simulated data for %d telescope(s)\n", hsdata->run_header.ntel);
//                if (verbose || rc != 0)
//                    printf("read_hess_runheader(), rc = %d\n", rc);
//                fprintf(stderr, "\nStarting run %d\n", hsdata->run_header.run);
//                if (showdata)
//                    print_hess_runheader(iobuf);

//                if (user_ana)
//                    do_user_ana(hsdata, item_header.type, 0);

                for (int itel = 0; itel < eventData.runHeader.ntel; itel++) {
                    int telId = eventData.runHeader.telId[itel];

                    // save local reference for easy of code
                    TelEvent telData = eventData.event.teldata[itel];
//                    hsdata->camera_set[itel].telId = telId;
//                    hsdata->camera_org[itel].telId = telId;
//                    hsdata->pixel_set[itel].telId = telId;
//                    hsdata->pixel_disabled[itel].telId = telId;
//                    hsdata->cam_soft_set[itel].telId = telId;
//                    hsdata->tracking_set[itel].telId = telId;
//                    hsdata->point_cor[itel].telId = telId;
                    eventData.event.numTel = eventData.runHeader.ntel;
                    eventData.event.trackdata[itel].telId = telId;

                    telData.telId = telId;
                    telData.raw = new AdcData();
//                    if ((hsdata->event.teldata[itel].raw =
//                            (AdcData *) calloc(1, sizeof(AdcData))) == NULL) {
//                        Warning("Not enough memory for AdcData");
//                        exit(1);
//                    }
                    telData.raw.telId = telId;

                    telData.pixtm = new PixelTiming();
//                    if ((hsdata->event.teldata[itel].pixtm =
//                            (PixelTiming *) calloc(1, sizeof(PixelTiming))) == NULL) {
//                        Warning("Not enough memory for PixelTiming");
//                        exit(1);
//                    }

                    telData.pixtm.telId = telId;

//                    if (do_calibrate && dst_level >= 0) /* Only when needed */
//                    {
//                        if ((hsdata->event.teldata[itel].pixcal =
//                                (PixelCalibrated *) calloc(1, sizeof(PixelCalibrated))) == NULL) {
//                            Warning("Not enough memory for PixelCalibrated");
//                            exit(1);
//                        }
//                        hsdata->event.teldata[itel].pixcal->telId = telId;
//                    }

                    telData.img = new ImgData[2];
                    telData.img[0] = new ImgData();
                    telData.img[0].telId = telId;
                    telData.img[1] = new ImgData();
                    telData.img[1].telId = telId;

                    telData.maxImageSets = 2;

                    eventData.event.teldata[itel] = telData;
//                    if ((hsdata->event.teldata[itel].img =
//                            (ImgData *) calloc(2, sizeof(ImgData))) == NULL) {
//                        Warning("Not enough memory for ImgData");
//                        exit(1);
//                    }

//                    hsdata->tel_moni[itel].tel_id = telId;
//                    hsdata->tel_lascal[itel].tel_id = telId;
                }

//                skip_run = 0;
//
//                if (only_runs.from != 0 || only_runs.to != 0) {
//                    if (!is_in_range(item_header.ident, &only_runs)) {
//                        skip_run = 1;
//                        printf("Ignoring data of run %ld\n", item_header.ident);
//                        if (nrun > 0)
//                            continue;
//                    }
//                }
//                if (is_in_range(item_header.ident, &not_runs)) {
//                    skip_run = 1;
//                    printf("Ignoring data of run %ld\n", item_header.ident);
//                    if (nrun > 0)
//                        continue;
//                }

                event = new CTAEvent(10, new byte[]{2, 3, 4});
            } else {
                header.findAndReadNextHeader();
                byte[] bytes = buffer.readBytes((int) header.length);
                event = new CTAEvent(0, bytes);
                header.getItemEnd();
            }
            item = DataFactory.create();
            item.put("@event", event);
        } else {
            log.info("Next sync marker has not been found.");
        }
        reverse = false;
        return item;
    }

    /**
     * Read the file 'EventioRegisteredNames.dat' and import the types mentioned there.
     */
    private void importEventioRegisteredDatatypes() {
        InputStream fileInput = EventIOStream.class.getResourceAsStream("/EventioRegisteredNames.dat");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput));
        String line;

        eventioTypes = new HashMap<>();
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] lineSplit = line.split(":");
                eventioTypes.put(Integer.valueOf(lineSplit[0]), lineSplit[1]);
            }
        } catch (IOException e) {
            log.error("Error while trying to read the EventioRegisteredNames.dat in order to import the datatypes.");
        }
    }

    private void setReverse(boolean reverse) {
        EventIOStream.reverse = reverse;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
