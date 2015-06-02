package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.*;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    // taken from FitsStream: start

    private final int MAX_HEADER_BYTES = 16 * 2880;
    private DataInputStream dataStream;

    @Parameter(
            required = false,
            description = "This value defines the size of the buffer of the BufferedInputStream",
            defaultValue = "8*1024")
    private int bufferSize = 8 * 1024;

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public EventIOStream (SourceURL url) {
        super(url);
    }

    public EventIOStream () {
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

        BufferedInputStream bStream = new BufferedInputStream(
                url.openStream(),
                bufferSize);
        dataStream = new DataInputStream(bStream);
        dataStream.mark(MAX_HEADER_BYTES);      // mark the start position

        // now we want to find the synchronisation marker
        // D41F8A37 or 378A1FD4
        boolean markerFound = true;
        int countMarkers = 0;
        while (markerFound) {
            markerFound = findSynchronisationMarker(dataStream);
            countMarkers++;
        }
        log.info("found " + countMarkers + " markers");
        // 4 bytes = 8 hex numbers
        // 1 byte = 8 bit = 2 * 4 bit = 2 * 1 hex
    }

    // taken from FitsStream: end

    /**
     *
     * @param dataStream
     * @return
     */
    private boolean findSynchronisationMarker(DataInputStream dataStream) {
        int firstBit = 0;
        int reverse = 1;
        int state = 0;
        String[] hexArray = {"d4", "1f", "8a", "37"};
        try {
            while (dataStream.available() > 0) {
                log.info("Still available " + dataStream.available());
                byte b = dataStream.readByte();
                int i = b & 0xFF;
                String hex = Integer.toHexString(i);
                if (firstBit == 0) {
                    if (hex.equals(hexArray[0])) {
                        firstBit = 1;
                        state = 1;
                    } else if (hex.equals(hexArray[3])) {
                        firstBit = 1;
                        state = 2;
                        reverse = -1;
                    }
                } else {
                    if (hex.equals(hexArray[state])) {
                        state += reverse;
                    }
                }
                if (state < 0 || state > 3) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Data readNext() throws Exception {
        return null;
    }
}
