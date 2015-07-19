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
import streams.cta.io.Event.FullEvent;
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

        // initialize data object containing all the parsed data from the stream
        eventData = new EventIOData();

        // import the registered types
        importEventioRegisteredDatatypes();
    }

    @Override
    public Data readNext() throws Exception {

        Data item = null;
        EventIOHeader header = new EventIOHeader(buffer);
        if (header.findAndReadNextHeader()) {

            //TODO add reading full event with what=-1

            CTAEvent event;
            // MC Shower
            if (header.type == 2020) {
                eventData.mcShower = MCShower.readMCShower(buffer, header);
                event = new CTAEvent(10, new byte[]{0, 1, 2});
            } else if (header.type == Constants.TYPE_EVENT) {
                eventData.event = new FullEvent().readFullEvent(buffer, header, -1);
                event = new CTAEvent(10, new byte[]{1, 2, 3,});
            } else {
                byte[] bytes = buffer.readBytes((int) header.length);
                event = new CTAEvent(0, bytes);
            }
            item = DataFactory.create();
            item.put("@event", event);
            header.getItemEnd();
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
