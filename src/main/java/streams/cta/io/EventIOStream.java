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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.cta.CTAEvent;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    boolean reverse = false;

    private DataInputStream dataStream;

    public HashMap<Integer, String> eventioTypes;

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

        BufferedInputStream bStream = new BufferedInputStream(
                url.openStream(),
                bufferSize);
        dataStream = new DataInputStream(bStream);

        // import the registered types
        importEventioRegisteredDatatypes();
    }

    @Override
    public Data readNext() throws Exception {
        // now we want to find the synchronisation marker
        // D41F8A37 or 378A1FD4
        boolean markerFound = findSynchronisationMarker(dataStream);

        // detect header if a top-level marker was found
        if (markerFound) {
            EventIOHeader header = new EventIOHeader(dataStream);
            header.readHeader();
            byte[] bytes = new byte[header.length];
            dataStream.read(bytes);
            CTAEvent event = new CTAEvent(0, bytes);
            Data item = DataFactory.create();
            item.put("@event", event);
            reverse = false;
            return item;
        }

        return null;
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

    /**
     * Try to find the next synchronisation marker "D41F8A37" or its reverse. In case the reverse
     * marker was found, the upcoming data block and its header should be handled in the proper way
     * (reverse the bytes).
     *
     * @param dataStream DataInputStream to be inspected
     * @return true, if marker was found; otherwise false.
     */
    private boolean findSynchronisationMarker(DataInputStream dataStream) {
        // TODO: can we make it faster by reading the stream byte by byte?
        // find_io_block in eventio.c
//        int firstBit = 0;
//        int reverse = 1;
//        int state = 0;
        byte[] bytes = new byte[4];
        int syncNormalOrderInt = 0xD41F8A37;
        int syncReverseOrderInt = 0x378A1FD4;
        try {
            while (dataStream.available() > 0) {
                dataStream.read(bytes);
                int intBytes = byteArrayToInt(bytes);
                if (intBytes == syncNormalOrderInt) {
                    return true;
                } else if (intBytes == syncReverseOrderInt) {
                    setReverse(true);
                    return true;
                }
//                int i = b & 0xFF;
//                String hex = Integer.toHexString(i);
//                if (firstBit == 0) {
//                    if (hex.equals(hexArray[0])) {
//                        firstBit = 1;
//                        state = 1;
//                    } else if (hex.equals(hexArray[3])) {
//                        firstBit = 1;
//                        state = 2;
//                        reverse = -1;
//                        log.info("Reverse datablock");
//                        setReverse(true);
//                    }
//                } else {
//                    if (hex.equals(hexArray[state])) {
//                        state += reverse;
//                    }
//                }
//
//                if (state < 0 || state > 3) {
//                    return true;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Calculate integer value of a byte array with a length of 4
     *
     * @param b byte array
     * @return integer value of a byte array
     */
    private int byteArrayToInt(byte[] b) {
        if (b.length != 4) {
            // TODO throw exception if this should happen?
            return 0;
        }
        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    private void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Header of EventIO file This class should read the header in front of the data block in
     * EventIO and determine some essential information as type, length and identification.
     */
    class EventIOHeader {

        int length;
        String type;
        String identification;
        DataInputStream dataInputStream;

        public EventIOHeader(DataInputStream dataInputStream) {
            this.dataInputStream = dataInputStream;
            length = -1;
            type = "";
            identification = "";
        }

        /**
         * Read EventIO defined header consisting of 3 - 4 fields of 4 bytes each
         */
        private void readHeader() throws IOException {
            //log.info("Datastream available: \t" + dataStream.available());

            byte[] bytes = new byte[4];

            // read type and check for extension field
            dataStream.read(bytes);
            int typeField = byteArrayToInt(bytes);

            // bits 0 to 15 are used for type information
            int type = typeField & 0x0000ffff;
            // TODO translate bytes to type. is it a string?

            // bit 16 is the user bit and is not set
            boolean user_flag = (typeField & 0x00010000) != 0;

            // extension is there if bit 17 is set
            // means look up if the 2nd bit of the 3rd byte is set
            boolean use_extension = ((typeField & 0x00020000) != 0);

            // bits 18 and 19 are reserved for future enhancements

            // bits 20 to 31 are used for the version information
            int version = (typeField >> 20) & 0xfff;

            // read identification field
            dataStream.read(bytes);
            int identField = byteArrayToInt(bytes);


            // read length
            dataStream.read(bytes);
            int lengthField = byteArrayToInt(bytes);

            // check whether bit 30 is set
            // meaning only subobjects are contained
            // and no elementary data types
            boolean onlySubObjects = (lengthField & 0x40000000) != 0;

            // check whether bit 31 is set
            // as it is a reserved bit it should be set to 0
            boolean reserved = (lengthField & 0x80000000) == 0;
            if (!reserved) {
                log.error("Reserved bit should be set to 0.");
                return;
            }

            // bits 0 to 29 are used for the length of the data block
            length = (lengthField & 0x3FFFFFFF);
            log.info("length:\t" + length + "\ttype:\t" + eventioTypes.get(type)
                    + "\tsubobjects:\t" + onlySubObjects);
            // TODO length parameter longer than the rest of the data?

            // read extension if given
            if (use_extension) {
                log.info("Extension exists.");
                // TODO dont skip
                dataStream.skipBytes(4);
            }
            //log.info("Data block length: \t" + length + "\n");
        }

    }
}
