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
import streams.cta.io.Event.FullEvent;
import streams.cta.io.Event.ImgData;
import streams.cta.io.Event.PixelTiming;
import streams.cta.io.Event.TelEvent;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    static boolean reverse = false;

    int numberEvents;

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
        BufferedInputStream bStream = new BufferedInputStream(url.openStream(), bufferSize);
        DataInputStream dataStream = new DataInputStream(bStream);

        // initialize buffer containing the data stream to read from it
        buffer = new EventIOBuffer(dataStream);

        // import the registered types
        importEventioRegisteredDatatypes();

        eventData = new EventIOData();
        numberEvents = 0;
    }

    @Override
    public Data readNext() throws Exception {

        Data item = null;
        EventIOHeader header = new EventIOHeader(buffer);
        if (header.findAndReadNextHeader(true)) {
            CTAEvent event;
            // MC Shower
            if (header.type == 2020) {
                if (!eventData.mcShower.readMCShower(buffer)) {
                    log.error("Error happened while reading MC Shower.");
                }
                event = new CTAEvent(10, new byte[]{0, 1, 2});
            } else if (header.type == Constants.TYPE_EVENT) {
                if (eventData.event == null) {
                    eventData.event = new FullEvent();
                }
                if (!eventData.event.readFullEvent(buffer, -1)) {
                    log.error("Error happened while reading full event data.");
                }
                numberEvents++;
                //TODO are we interested in some postprocessing as in original code?

                event = new CTAEvent(10, new byte[]{1, 2, 3,});
            } else if (header.type == 2000) {

                // Summary of a preceding run in the same file ?
//                if (!quiet && hsdata != NULL && eventData.runHeader.run > 0)
//                    show_run_summary(hsdata, nev, ntrg, plidx, wsum_all, wsum_trg,
//                            rmax_x, rmax_y, rmax_r);
//                else if (nev > 0)
//                    printf("%d of %d events triggered.\n", ntrg, nev);

                        /* Free main structure */
//                    if (!dst_processing) {
//                        free(hsdata);
//                        hsdata = NULL;
//                    }

//                nev = ntrg = 0;
//                wsum_all = wsum_trg = 0.;

//                nrun++;

                if (!eventData.runHeader.readRunHeader(buffer)) {
                    log.error("Error happened while reading run header.");
                    return null;
                }

                eventData.event = new FullEvent();

//                if (!quiet)
//                    printf("Reading simulated data for %d telescope(s)\n", eventData.runHeader.ntel);
//                if (verbose || rc != 0)
//                    printf("read_hess_runheader(), rc = %d\n", rc);
//                fprintf(stderr, "\nStarting run %d\n", eventData.runHeader.run);
//                if (showdata)
//                    print_hess_runheader(iobuf);

//                if (user_ana)
//                    do_user_ana(hsdata, item_header.type, 0);

                //TODO ntel > H_MAX_TEL!?
                for (int itel = 0; itel < eventData.runHeader.ntel; itel++) {
                    int telId = eventData.runHeader.telId[itel];

                    // save local reference for easy of code
                    TelEvent telData = eventData.event.teldata[itel];
//                    camera_set[itel].telId = telId;
//                    camera_org[itel].telId = telId;
//                    pixel_set[itel].telId = telId;
//                    pixel_disabled[itel].telId = telId;
//                    cam_soft_set[itel].telId = telId;
//                    tracking_set[itel].telId = telId;
//                    point_cor[itel].telId = telId;
                    eventData.event.numTel = eventData.runHeader.ntel;
                    eventData.event.trackdata[itel].telId = telId;

                    telData.telId = telId;

                    //TODO originally one is trying to check whether this objects fits in the memory!
                    telData.raw = new AdcData();
                    telData.raw.telId = telId;

                    telData.pixtm = new PixelTiming();
                    telData.pixtm.telId = telId;

//                    if (do_calibrate && dst_level >= 0) /* Only when needed */
//                    {
//                        if ((event.teldata[itel].pixcal =
//                                (PixelCalibrated *) calloc(1, sizeof(PixelCalibrated))) == NULL) {
//                            Warning("Not enough memory for PixelCalibrated");
//                            exit(1);
//                        }
//                        event.teldata[itel].pixcal->telId = telId;
//                    }

                    telData.img = new ImgData[2];
                    telData.img[0] = new ImgData();
                    telData.img[0].telId = telId;
                    telData.img[1] = new ImgData();
                    telData.img[1].telId = telId;

                    telData.maxImageSets = 2;

                    eventData.event.teldata[itel] = telData;

//                    tel_moni[itel].tel_id = telId;
//                    tel_lascal[itel].tel_id = telId;
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
            log.info("Next sync marker has not been found: \nstill available datastream :" + buffer.dataStream.available());
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
