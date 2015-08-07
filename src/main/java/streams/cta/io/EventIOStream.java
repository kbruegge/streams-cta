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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;
import streams.cta.Constants;
import streams.cta.io.event.FullEvent;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    static int byteOrder = 0;

    int numberEvents;

    int numberRuns;

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

        // initialize data object containing the data read from EventIO file
        eventData = new EventIOData();

        // import the registered types
        importEventioRegisteredDatatypes();

        numberEvents = 0;
    }

    @Override
    public Data readNext() throws Exception {

        Data item;
        boolean eventFound = false;
        numberRuns = 0;
        item = DataFactory.create();
        while (!eventFound) {
            numberRuns++;
            EventIOHeader header = new EventIOHeader(buffer);
            if (header.findAndReadNextHeader(true)) {
                if (header.type == Constants.TYPE_MCSHOWER) {
                    if (!eventData.mcShower.readMCShower(buffer)) {
                        log.error("Error happened while reading MC Shower.");
                    }
                } else if (header.type == Constants.TYPE_EVENT) {
                    if (eventData.event == null) {
                        eventData.event = new FullEvent();
                    }
                    if (!eventData.event.readFullEvent(buffer, -1)) {
                        log.error("Error happened while reading full event data.");
                    }
                    numberEvents++;
                    eventFound = true;

                    //TODO: add more telescope data into the item
                    item.put("@raw_data", eventData.event.teldata[0].raw.adcSample[0]);
                    long seconds = eventData.event.central.gpsTime.seconds;
                    long nanoseconds = eventData.event.central.gpsTime.nanoseconds;
                    item.put("@timestamp", LocalDateTime.ofEpochSecond(seconds, (int) nanoseconds, ZoneOffset.UTC));
                    item.put("@telescope", new CTATelescope(CTATelescopeType.LST, 12, 0, 0, 0, null, null, null));
                } else if (header.type == Constants.TYPE_RUNHEADER) {

                    //TODO some summary from previous runs (original code)

                    if (!eventData.runHeader.readRunHeader(buffer)) {
                        log.error("Error happened while reading run header.");
                        return null;
                    }

                    eventData.event = initFullEvent(eventData.runHeader.numberTelescopes);

                    //TODO skip some runs
                } else {
                    header.findAndReadNextHeader();
                    buffer.skipBytes((int) header.length);
                    header.getItemEnd();
                }

            } else {
                log.info("Next sync marker has not been found: \nstill available datastream :"
                        + buffer.dataStream.available());
            }
        }
        byteOrder = Constants.LITTLE_ENDIAN;
        log.info("Event number " + numberEvents + "\tNumber runs: " + numberRuns);
        return item;
    }

    /**
     * Initialize the FullEvent object containing all the different information about an event.
     *
     * @param numberTelescopes number of telescopes
     * @return FullEvent object
     */
    public FullEvent initFullEvent(int numberTelescopes) {
        FullEvent event = new FullEvent();

        //TODO do_user_ana from original code

        event.numTel = numberTelescopes;
        //TODO numberTelescopes > H_MAX_TEL!?
        event.triggeredTelescopeIds = eventData.runHeader.telId;
//        for (int itel = 0; itel < numberTelescopes; itel++) {
//            short telId = eventData.runHeader.telId[itel];

//            camera_set[itel].telId = telId;
//            camera_org[itel].telId = telId;
//            pixel_set[itel].telId = telId;
//            pixel_disabled[itel].telId = telId;
//            cam_soft_set[itel].telId = telId;
//            tracking_set[itel].telId = telId;
//            point_cor[itel].telId = telId;

        //TODO do some calibration

//            tel_moni[itel].tel_id = telId;
//            tel_lascal[itel].tel_id = telId;
//        }
        return event;
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
            log.error("Error while trying to read the EventioRegisteredNames.dat " +
                    "in order to import the datatypes.");
        }
    }

    private void setReverse(int byteOrder) {
        EventIOStream.byteOrder = byteOrder;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
