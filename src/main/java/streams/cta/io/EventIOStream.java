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

    boolean reverse = false;

    // taken from FitsStream: start

    private final int MAX_HEADER_BYTES = 16 * 2880;
    private DataInputStream dataStream;

    @Parameter(
            required = false,
            description = "This value defines the size of the buffer of the BufferedInputStream",
            defaultValue = "8*1024")

    private int bufferSize = 8 * 1024;

    public EventIOStream (SourceURL url) {
        super(url);
    }

    public EventIOStream () {
        super();
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
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

            // detect header if a top-level marker was found
            if (markerFound) {
                readHeader();
            }
            countMarkers++;
        }
        log.info("found " + countMarkers + " markers");
    }

    // taken from FitsStream: end

    /**
     * Read EventIO defined header consisting of 3 - 4 fields of 4 bytes each
     *
     * @throws IOException
     */
    private void readHeader() throws IOException {
        log.info("Datastream available: \t" + dataStream.available());

        byte[] bytes = new byte[4];

        // read type and check for extension field
        dataStream.read(bytes);
        if (reverse) {
            bytes = reverseByteArray(bytes);
        }

        // extension is there if bit 17 is set
        // means look up if the 2nd bit of the 3rd byte is set
        boolean extended = ((bytes[2] >> 1) & 1) == 1;


        // read identification field
        // TODO dont skip
        dataStream.skipBytes(4);


        // read length
        dataStream.read(bytes);
        if (reverse) {
            bytes = reverseByteArray(bytes);
        }

        // check whether bit 30 is set
        // meaning only subobjects are contained
        // and no elementary data types
        boolean onlySubObjects = ((bytes[3] >> 6) & 1) == 1;
        log.info("Only sub-objects bit set: " + onlySubObjects);

        // check whether bit 31 is set
        // as it is a reserved bit it should be set to 0
        boolean reserved = ((bytes[3] >> 7) & 1) == 0;
        log.info("Reserved length-bit set: " + reserved);
        // TODO throw exception if reserved bit is set to 1?

        // set the two last bits to 0
        // and then inspect the whole bytes to detect
        // the length of the data block
        bytes[3] = (byte) (bytes[3] & ~(1 << 6));
        bytes[3] = (byte) (bytes[3] & ~(1 << 7));

        if (reverse) {
            bytes = reverseByteArray(bytes);
        }
        int length = byteArrayToInt(bytes);
        // TODO length parameter longer than the rest of the data?

        // read extension if given
        if (extended) {
            log.info("Extension exists.");
            // TODO dont skip
            dataStream.skipBytes(4);
        }

        log.info("Data block length: \t" + length + "\n");

        // TODO dont skip
        dataStream.skipBytes(length);
    }

    /**
     * Calculate integer value of a byte array with a length of 4
     * @param b byte array
     * @return integer value of a byte array
     */
    private int byteArrayToInt(byte[] b) {
        if (b.length != 4) {
            // TODO throw exception if this should happen?
            return 0;
        }
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     * Reverse a byte array.
     *
     * @param b byte array
     * @return reversed byte array
     */
    private byte[] reverseByteArray(byte[] b) {
        for(int i = 0; i < b.length / 2; i++)
        {
            byte temp = b[i];
            b[i] = b[b.length - i - 1];
            b[b.length - i - 1] = temp;
        }
        return b;
    }

    /**
     * Try to find the next synchronisation marker "D41F8A37" or its reverse.
     * In case the reverse marker was found, the upcoming data block and
     * its header should be handled in the proper way (reverse the bytes).
     *
     * @param dataStream DataInputStream to be inspected
     * @return true, if marker was found; otherwise false.
     */
    private boolean findSynchronisationMarker(DataInputStream dataStream) {
        int firstBit = 0;
        int reverse = 1;
        int state = 0;
        String[] hexArray = {"d4", "1f", "8a", "37"};
        try {
            while (dataStream.available() > 0) {
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
                        log.info("Reverse datablock");
                        setReverse(true);
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

    private void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    public Data readNext() throws Exception {
        return null;
    }
}
