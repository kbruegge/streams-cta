package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import streams.cta.Constants;

/**
 * Header of EventIO file This class should read the header in front of the data block in EventIO
 * and determine some essential information as typeString, length and identification.
 */
public class EventIOHeader {

    static Logger log = LoggerFactory.getLogger(EventIOHeader.class);

    /**
     * Length of data field, for information only.
     */
    long length;

    /**
     * The typeString number telling the typeString of I/O block.
     */
    int type;

    /**
     * The version number used for the block.
     */
    long version;

    /**
     * Identity number.
     */
    long identification;

    /**
     * Extension number
     */
    long extension;

    /**
     * Tells how many levels deep we are nested now.
     */
    int level;

    /**
     * One more bit in the header available for user data.
     */
    boolean userFlag;

    /**
     * Non-zero if the extension header field should be used.
     */
    boolean useExtension;

    boolean onlySubObjects;

    String typeString;
    EventIOBuffer buffer;

    public EventIOHeader(EventIOBuffer buffer) {
        this.buffer = buffer;
        length = -1;
        version = -1;
        typeString = "";
    }

    public boolean findAndReadNextHeader(boolean reset) throws IOException {
        //TODO use the wanted type and controll it
        long wantedType;

        //TODO use the right constant
        if (buffer.itemLevel >= 100) {
            log.error("Maximum level of sub-items in I/O Buffer exceeded.");
            //TODO: what to do now?
        }


        if (buffer.itemLevel > 0) {
            //TODO do the check like in eventio.c line 3192
        } else if (buffer.itemLevel == 0 && !buffer.syncMarkerFound) {
            EventIOStream.reverse = false;
            boolean found = findSynchronisationMarker();
            if (!found) {
                log.info("Synchronisation marker could not have been found.");
                return false;
            }
        }

        if (reset) {
            buffer.dataStream.mark(100);
        }

        wantedType = type;

        // read typeString and check for extension field
        int typeField = buffer.readLong();

        // bits 0 to 15 are used for typeString information
        type = typeField & 0x0000ffff;
        typeString = EventIOStream.eventioTypes.get(type);

        // bit 16 is the user bit and is not set
        userFlag = (typeField & 0x00010000) != 0;

        // extension is there if bit 17 is set
        // means look up if the 2nd bit of the 3rd byte is set
        useExtension = ((typeField & 0x00020000) != 0);

        // bits 18 and 19 are reserved for future enhancements

        // bits 20 to 31 are used for the version information
        version = (typeField >> 20) & 0xfff;

        // Encountering corrupted data seems more likely than having version numbers above 2047
        if ((version & 0x800) != 0) {
            log.error("Version number invalid: may be corrupted data:\t" + version);
            return false;
        }

        // read identification field
        identification = buffer.readLong();

        // read length
        long lengthField = buffer.readUnsignedInt32();

        // check whether bit 30 is set
        // meaning only subobjects are contained
        // and no elementary data types
        onlySubObjects = (lengthField & 0x40000000) != 0;

        // check whether bit 31 is set
        // as it is a reserved bit it should be set to 0
//        boolean reserved = (lengthField & 0x80000000) == 0;
//        if (!reserved) {
//            log.error("Reserved bit should be set to 0.");
//            return false;
//        }

        if (buffer.itemLevel == 0) {
            buffer.readLengthLocal[buffer.itemLevel] -= 12;
        }

        // read extension if given
        if ((lengthField & 0x80000000) != 0) {
            log.info("Extension exists.");
            useExtension = true;
            extension = buffer.readUnsignedInt32();
            if (buffer.itemLevel == 0) {
                buffer.readLengthLocal[buffer.itemLevel] -= 4;
            }
            // Actual length consists of bits 0-29 of length field plus bits 0-11 of extension field.
            length = (lengthField & 0x3FFFFFFF) | ((extension & 0x0FFF) << 30);
        } else {
            // bits 0 to 29 are used for the length of the data block
            length = (lengthField & 0x3FFFFFFF);
        }

        if (!reset) {
            // save the length of the item
            buffer.itemLength[buffer.itemLevel] = length;

            if (onlySubObjects) {
                buffer.subItemLength[buffer.itemLevel] = length;
            } else {
                buffer.subItemLength[buffer.itemLevel] = 0;
            }

            // For global offsets keep also track where header extensions were found.
            buffer.itemExtension[buffer.itemLevel] = useExtension;
        }

        if (wantedType > 0 && wantedType != type) {
            // TODO what is if the type is wrong?! skip the item?
            log.error("Wrong datatype: " + wantedType + " was expected, but "
                    + type + " was read.");
        }

        if (!reset) {
            level = buffer.itemLevel++;
        } else {
            buffer.dataStream.reset();
        }
        return true;
    }

    /**
     * Searches the stream for the sync marker and reads EventIO defined header consisting of 3 - 4
     * fields of 4 bytes each
     */
    public boolean findAndReadNextHeader() throws IOException {
        return findAndReadNextHeader(false);
    }

    /**
     * Try to find the next synchronisation marker "D41F8A37" or its reverse. In case the reverse
     * marker was found, the upcoming data block and its header should be handled in the proper way
     * (reverse the bytes).
     *
     * @return true, if marker was found; otherwise false.
     */
    private boolean findSynchronisationMarker() {
        // find_io_block in eventio.c
        int firstBit = 0;
        int reverse = 1;
        int state = 0;

        int[] syncMarker = {0xD4, 0x1F, 0x8A, 0x37};

        try {
            while (buffer.dataStream.available() > 0) {
                byte b = buffer.dataStream.readByte();

                if (firstBit == 0) {
                    if (b == syncMarker[0]) {
                        firstBit = 1;
                        state = 1;
                    } else if (b == syncMarker[3]) {
                        firstBit = 1;
                        state = 2;
                        reverse = -1;
                        EventIOStream.reverse = true;
                    }
                } else {
                    if (b == (byte) syncMarker[state]) {
                        state += reverse;
                    } else {
                        firstBit = 0;
                        state = 0;
                        reverse = 1;
                    }
                }

                if (state < 0 || state > 3) {
                    buffer.syncMarkerFound = true;
                    return true;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Find the end of the current item. Update the level if you're leaving one level and skip the
     * bytes left til the data length.
     */
    public void getItemEnd() {
        //TODO return boolean if finding item end was successful
        int ilevel = -1;

        if (level != buffer.itemLevel - 1) {
            if (level >= buffer.itemLevel) {
                log.error("Attempt to finish getting an item which is not active: " + type);
            } else {
                log.error("Item level is inconsistent.");
            }
        }


        int localReadLength = buffer.readLengthLocal[buffer.itemLevel];
        for (int i = 0; i < buffer.itemLevel; i++) {
            buffer.readLength[i] += length + 12 + (useExtension ? 4 : 0);
            buffer.readLengthLocal[i] += localReadLength;
        }
        buffer.readLength[level] = 0;

        //buffer.readLengthLocal[buffer.itemLevel - 1] += localReadLength;
        buffer.readLengthLocal[buffer.itemLevel] = 0;

        if (level >= 0 && level <= Constants.MAX_IO_ITEM_LEVEL) {
            if (level == 0 & buffer.itemLevel == 1) {
                buffer.syncMarkerFound = false;
            }

            ilevel = level;
            buffer.itemLevel = level;
        } else {
            //TODO: something is wrong
        }

        /* If the item has a length specified, check it. */
        if (buffer.itemLength[ilevel] >= 0) {
            //TODO check whether the length that has been read matches the predefined length

            if (buffer.itemLength[buffer.itemLevel] != buffer.readLength[buffer.itemLevel]) {
                if (length > buffer.itemLength[buffer.itemLevel]) {
                    log.error("Actual length of item type " + type + " exceeds specified length");
                }

                // calculate how much of the byte stream real length has been read
                // and skip the rest of it until the next item
                int skipLength = (int) length - localReadLength;
                buffer.skipBytes(skipLength);

            }
        }

        if (buffer.itemLevel == 0) {
            // TODO stop reading!!
        }
    }

    public long getVersion() {
        return version;
    }

    public long getLength() {
        return length;
    }

    public long getIdentification() {
        return identification;
    }

    public int getType() {
        return type;
    }
}
