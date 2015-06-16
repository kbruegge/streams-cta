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

    //TODO: move to constants file
    int MAX_IO_ITEM_LEVEL = 20;

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    boolean reverse = false;

    private DataInputStream dataStream;

    public HashMap<Integer, String> eventioTypes;

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

        buffer = new EventIOBuffer();
        eventData = new EventIOData();

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
        Data item = null;

        // detect header if a top-level marker was found
        if (markerFound) {
            EventIOHeader header = new EventIOHeader(dataStream);
            header.readHeader(buffer);

            // MC Shower
            if (header.type == 2020) {
                eventData.mcShower = readMCShower(dataStream, header);
                item = DataFactory.create();
                CTAEvent event = new CTAEvent(10, new byte[]{0, 1, 2});
                item.put("@event", event);
            } else {
                byte[] bytes = new byte[header.length];
                dataStream.read(bytes);
                CTAEvent event = new CTAEvent(0, bytes);
                item = DataFactory.create();
                item.put("@event", event);
            }
            header.getItemEnd(buffer, dataStream);
            reverse = false;
            return item;
        }

        return null;
    }

    private MCShower readMCShower(DataInputStream dataStream, EventIOHeader header)
            throws IOException {
        if (header.version > 2) {
            log.error("Unsupported MC shower version: " + header.version);
            dataStream.skipBytes(header.length);
            return null;
        }

        MCShower mcShower = new MCShower();
        mcShower.shower_num = header.identification;

        mcShower.primary_id = readInt32(dataStream); // int32
        mcShower.energy = readReal(dataStream); // real
        mcShower.azimuth = readReal(dataStream); // real
        mcShower.altitude = readReal(dataStream); // real
        if (header.version >= 1) {
            mcShower.depth_start = readReal(dataStream); // real
        }
        mcShower.h_first_int = readReal(dataStream);
        mcShower.xmax = readReal(dataStream);
        mcShower.hmax = mcShower.emax = mcShower.cmax = 0d;

        if (header.version >= 1) {
            mcShower.hmax = readReal(dataStream);
            mcShower.emax = readReal(dataStream);
            mcShower.cmax = readReal(dataStream);
        }

        mcShower.num_profiles = readInt16(dataStream); // short

        //TODO: this should be in a file containing all constants
        int H_MAX_PROFILE = 10;

        for (int i = 0; i < mcShower.num_profiles && i < H_MAX_PROFILE; i++) {
            int skip = 0;
            ShowerProfile profile = new ShowerProfile();
            profile.id = readInt32(dataStream);
            profile.num_steps = readInt32(dataStream);
            if (profile.num_steps > profile.max_steps) {
                if (profile.content != null) {
                    if (profile.max_steps > 0) {
                        profile.content = null;
                    } else {
                        skip = 1;
                    }
                }
            }

            profile.start = readReal(dataStream);
            profile.end = readReal(dataStream);

            if (profile.num_steps > 0) {
                profile.binsize = (profile.end - profile.start) / (double) profile.num_steps;
            }
            if (profile.content == null) {
                profile.content = new double[profile.num_steps];

                // here in original code there is a check
                // whether content could have been allocated
                // otherwise there were too little space

                profile.max_steps = profile.num_steps;
            }

            if (skip == 1) {
                for (int j = 0; j < profile.num_steps; j++) {
                    readReal(dataStream);
                }
                profile.num_steps *= -1;
            } else {
                profile.content = readVectorOfReals(profile.num_steps, dataStream);
            }
            mcShower.profile[i] = profile;
        }

        if (header.version >= 2) {
            mcShower.extra_parameters = readShowerExtraParameters(dataStream, header);
        } else {
            clearShowerExtraParameters(mcShower.extra_parameters);
        }
        return mcShower;
    }

    private void clearShowerExtraParameters(ShowerExtraParameters extra_parameters) {
        //TODO: implement
        extra_parameters.id = 0;
        extra_parameters.is_set = 0;
        extra_parameters.weight = 1.0;

//        if ( ep->iparam != NULL )
//        {
//            for (i=0; i<ep->niparam; i++)
//                ep->iparam[i] = 0;
//        }
//        if ( ep->fparam != NULL )
//        {
//            for (i=0; i<ep->nfparam; i++)
//                ep->fparam[i] = 0;
//        }
    }

    private ShowerExtraParameters readShowerExtraParameters(DataInputStream dataStream, EventIOHeader header) throws IOException {
        //TODO: implement
        ShowerExtraParameters ep = new ShowerExtraParameters();
        ep.is_set = 0;

        //TODO: get begin of an item

        //TODO: go to the end if version != 1

        ep.id = header.identification;
        ep.weight = readReal(dataStream);


        return null;
    }

    private double[] readVectorOfReals(int vectorSize, DataInputStream dataStream)
            throws IOException {
        double[] vector = new double[vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            vector[i] = readReal(dataStream);
        }
        return vector;
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

    //TODO: use float here?
    private double readReal(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return ByteBuffer.wrap(b).getFloat();
        }
    }

    private double readDouble(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        } else {
            return ByteBuffer.wrap(b).getDouble();
        }
    }

    private int readLong(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    private short readInt16(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[2];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } else {
            return ByteBuffer.wrap(b).getShort();
        }
    }

    private int readInt32(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    private long readInt64(DataInputStream dataStream) throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            return ByteBuffer.wrap(b).getLong();
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

    private void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Header of EventIO file This class should read the header in front of the data block in
     * EventIO and determine some essential information as typeString, length and identification.
     */
    class EventIOHeader {


        /**
         * < Length of data field, for information only.
         */
        int length;

        /**
         * < The typeString number telling the typeString of I/O block.
         */
        int type;

        /**
         * < The version number used for the block.
         */
        long version;

        /**
         * < Identity number.
         */
        long identification;

        /**
         * < Tells how many levels deep we are nested now.
         */
        int level;

        /**
         * < One more bit in the header available for user data.
         */
        boolean userFlag;

        /**
         * < Non-zero if the extension header field should be used.
         */
        boolean useExtension;

        boolean onlySubObjects;

        String typeString;
        DataInputStream dataInputStream;

        public EventIOHeader(DataInputStream dataInputStream) {
            this.dataInputStream = dataInputStream;
            length = -1;
            version = -1;
            typeString = "";
        }

        /**
         * Read EventIO defined header consisting of 3 - 4 fields of 4 bytes each
         */
        private void readHeader(EventIOBuffer buffer) throws IOException {

            long thisType;
            long wantedType;
            int ilevel = buffer.itemLevel;
            long previousRemaining;
            int previousLevel, previousOrder;

            previousLevel = buffer.itemLevel;

            //TODO use the right constant
            if (ilevel >= 100){
                log.error("Maximum level of sub-items in I/O Buffer exceeded.");
                //TODO: what to do now?
            }

            if (ilevel > 0){
                //TODO do the check like in eventio.c line 3192
            } else if (ilevel == 0){
                reverse = false;
                findSynchronisationMarker(dataStream);
            }

            wantedType = type;

            //log.info("Datastream available: \t" + dataStream.available());

            // read typeString and check for extension field
            int typeField = readLong(dataStream);

            // bits 0 to 15 are used for typeString information
            type = typeField & 0x0000ffff;
            typeString = eventioTypes.get(type);

            // bit 16 is the user bit and is not set
            userFlag = (typeField & 0x00010000) != 0;

            // extension is there if bit 17 is set
            // means look up if the 2nd bit of the 3rd byte is set
            useExtension = ((typeField & 0x00020000) != 0);

            // bits 18 and 19 are reserved for future enhancements

            // bits 20 to 31 are used for the version information
            version = (typeField >> 20) & 0xfff;

            // read identification field
            identification = readLong(dataStream);

            // read length
            int lengthField = readInt32(dataStream);

            // check whether bit 30 is set
            // meaning only subobjects are contained
            // and no elementary data types
            onlySubObjects = (lengthField & 0x40000000) != 0;

            // check whether bit 31 is set
            // as it is a reserved bit it should be set to 0
            boolean reserved = (lengthField & 0x80000000) == 0;
            if (!reserved) {
                log.error("Reserved bit should be set to 0.");
                return;
            }

            // bits 0 to 29 are used for the length of the data block
            length = (lengthField & 0x3FFFFFFF);

            buffer.itemLength[buffer.itemLevel] = length;

            if (onlySubObjects){
                buffer.subItemLength[buffer.itemLevel] = length;
            } else{
                buffer.subItemLength[buffer.itemLevel] = 0;
            }

            buffer.itemExtension[buffer.itemLevel] = useExtension;

            log.info("length:\t" + length + "\ttypeString:\t" + typeString
                    + "\tsubobjects:\t" + onlySubObjects);
            // TODO length parameter longer than the rest of the data?

            // read extension if given
            if (useExtension) {
                log.info("Extension exists.");
                // TODO dont skip
                dataStream.skipBytes(4);
            }

            if (wantedType > 0 && wantedType != type){
                // TODO what is if the type is wrong?! skip the item?
                log.error("Wrong datatype: " + wantedType + " was expected, but "
                        + type + " was read.");
            }

            level = buffer.itemLevel++;

        }

        public void getItemEnd(EventIOBuffer buffer, DataInputStream dataStream){
            long localLength;
            int ilevel = -1;

            if (level != buffer.itemLevel -1){
                if (level >= buffer.itemLevel){
                    log.error("Attempt to finish getting an item which is not active.");
                }else{
                    log.error("Item level is inconsistent.");
                }
            }

            if (level >= 0 && level <= MAX_IO_ITEM_LEVEL ){
                ilevel = buffer.itemLevel = level;
            }else{
                //TODO: something is wrong
            }

            /* If the item has a length specified, check it. */
            if (buffer.itemLength[ilevel] >= 0){
                //TODO check whether the length that has been read matches the predefined length
            }

            if (buffer.itemLevel == 0){
                // TODO stop reading!!
            }
        }
    }

    class EventIOBuffer {

        /**
         * Current level of nesting of items.
         */
        int itemLevel;
        /**
         * Length of each level of items
         */
        long[] itemLength;
        /**
         * Length of its sub-items
         */
        long[] subItemLength;
        /**
         * Where the item starts in buffer.
         */
        long[] itemStartOffset;
        /**
         * Where the extension field was used.
         */
        boolean[] itemExtension;
        /**
         * Set if block is not in internal byte order.
         */
        int dataPending;
        /**
         * Count of synchronization errors.
         */
        int sync_err_count;
        /**
         * Maximum accepted number of synchronisation errors.
         */
        int sync_err_max;

        public EventIOBuffer() {
            itemLength = new long[MAX_IO_ITEM_LEVEL];
            subItemLength = new long[MAX_IO_ITEM_LEVEL];
            itemStartOffset = new long[MAX_IO_ITEM_LEVEL];
            itemExtension = new boolean[MAX_IO_ITEM_LEVEL];
        }
    }

    private class EventIOData {
        MCShower mcShower;
    }
}
