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
    }

    // taken from FitsStream: end

    @Override
    public Data readNext() throws Exception {
        // now we want to find the synchronisation marker
        // D41F8A37 or 378A1FD4
        boolean markerFound = findSynchronisationMarker(dataStream);

        // detect header if a top-level marker was found
        if (markerFound) {
            EventIOHeader header = new EventIOHeader(dataStream);
            header.readHeader();
            // TODO dont skip
            byte[] bytes = new byte[header.length];
            dataStream.read(bytes); // skipBytes(header.length);
            CTAEvent event = new CTAEvent(0, bytes);
            Data item = DataFactory.create();
            item.put("@event", event);
            reverse = false;
            return item;
        }

        return null;
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
        if (reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    private void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Header of EventIO file
     * This class should read the header in front of the
     * data block in EventIO and determine some essential information
     * as type, length and identification.
     */
    class EventIOHeader {

        int length;
        String type;
        String identification;
        DataInputStream dataInputStream;

        public EventIOHeader (DataInputStream dataInputStream) {
            this.dataInputStream = dataInputStream;
            length = -1;
            type = "";
            identification = "";
        }

        /**
         * Read EventIO defined header consisting of 3 - 4 fields of 4 bytes each
         *
         * @throws IOException
         */
        private void readHeader() throws IOException {
//            /* Remember the requested item type. */
//            wanted_type = item_header->type;
//   /* Extract the actual type and version from the 'type/version' field. */
//            this_type = (unsigned long) get_long(iobuf);
//            item_header->type = this_type & 0x0000ffffUL;
//            item_header->version = (unsigned) (this_type >> 20) & 0xfff;
//            if ( (item_header->version & 0x800) != 0 )
//            {
//      /* Encountering corrupted data seems more likely than having version numbers above 2047 */
//                Warning("Version number invalid - may be corrupted data");
//                return -1;
//            }
//            item_header->user_flag = ((this_type & 0x00010000UL) != 0);
//            item_header->use_extension = ((this_type & 0x00020000UL) != 0);
//
//   /* Extract the identification number */
//            item_header->ident = get_long(iobuf);
//
//   /* If bit 30 of length is set the item consists only of sub-items. */
//            length = get_uint32(iobuf);
//            if ( (length & 0x40000000UL) != 0 )
//            item_header->can_search = 1;
//            else
//            item_header->can_search = 0;
//            if ( (length & 0x80000000UL) != 0 )
//            {
//                item_header->use_extension = 1;
//      /* Check again that we are not beyond the superior item after reading the extension */
//                if ( ilevel > 0 &&
//                        (long) (iobuf->data-iobuf->buffer) + 16 >=
//                                iobuf->item_start_offset[ilevel-1] + iobuf->item_length[ilevel-1] )
//                    return -2;
//                extension = get_uint32(iobuf);
//      /* Actual length consists of bits 0-29 of length field plus bits 0-11 of extension field. */
//                length = (length & 0x3FFFFFFFUL) | ((extension & 0x0FFFUL) << 30);
//            }
//            else
//            length = (length & 0x3FFFFFFFUL);
//            item_header->length = length;
//            iobuf->item_length[ilevel] = (long) length;
//            if ( item_header->can_search )
//                iobuf->sub_item_length[ilevel] = (long) length;
//            else
//                iobuf->sub_item_length[ilevel] = 0;


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
            //boolean onlySubObjects = ((bytes[3] >> 6) & 1) == 1;
            boolean onlySubObjects = (lengthField & 0x40000000) != 0;
            //log.info("sub-objects:\t" + onlySubObjects + "\treal:\t" + onlySubObjectsReal);

            // check whether bit 31 is set
            // as it is a reserved bit it should be set to 0
            boolean reserved = (lengthField & 0x80000000) == 0;
            if (!reserved) {
                log.error("Reserved bit should be set to 0.");
                return;
            }

            // set the two last bits to 0
            // and then inspect the whole bytes to detect
            // the length of the data block
//            bytes[3] = (byte) (bytes[3] & ~(1 << 6));
//            bytes[3] = (byte) (bytes[3] & ~(1 << 7));

            //length = (length & 0x3FFFFFFF) | ((extension & 0x0FFF) << 30);
            //length = byteArrayToInt(bytes);
            length = (lengthField & 0x3FFFFFFF);
            //log.info("length:\t" + length + "\treal:\t" + lengthReal);

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
