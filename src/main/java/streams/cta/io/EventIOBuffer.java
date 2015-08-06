package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static streams.cta.Constants.H_MAX_TEL;
import static streams.cta.Constants.MAX_HEADER_SIZE;
import static streams.cta.Constants.MAX_IO_ITEM_LEVEL;

/**
 * Created by alexey on 17.06.15.
 */
public class EventIOBuffer {

    static Logger log = LoggerFactory.getLogger(EventIOBuffer.class);

    /**
     * Current level of nesting of items.
     */
    int itemLevel;

    /**
     * Length of each level of items
     */
    long[] itemLength;

    String[] itemType;

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
    int syncErrCount;

    /**
     * Maximum accepted number of synchronisation errors.
     */
    int syncErrMax;

    /**
     * Count the length of a byte stream that has been read.
     */
    int[] readLength;

    static int[][] gTelIdx = new int[3][H_MAX_TEL + 1];
    static int[] gTelIdxInit = new int[3];

    /**
     * Reference number when dealing with multiple telescope lookup tables.
     */
    static int gTelIdxRef = 0;

    public boolean syncMarkerFound = false;

    DataInputStream dataStream;

    public EventIOBuffer(DataInputStream dataStream) {
        itemLength = new long[MAX_IO_ITEM_LEVEL];
        subItemLength = new long[MAX_IO_ITEM_LEVEL];
        itemStartOffset = new long[MAX_IO_ITEM_LEVEL];
        itemExtension = new boolean[MAX_IO_ITEM_LEVEL];
        this.dataStream = dataStream;
        //readLength = new int[MAX_IO_ITEM_LEVEL];
        readLength = new int[MAX_IO_ITEM_LEVEL];
        itemType = new String[MAX_IO_ITEM_LEVEL];
    }

    public void skipBytes(int length) {
        try {
            dataStream.skipBytes(length);
            readLength[itemLevel] += length;
        } catch (IOException e) {
            log.error("Skipping bytes produced an error:\n" + e.getMessage());
        }
    }

    /**
     * Read the header of a sub-item, recognize the type of it and reset the stream back.
     *
     * @return type of a sub-item
     */
    public int nextSubitemType() {
        dataStream.mark(MAX_HEADER_SIZE);

        // Are we beyond the last sub-item?
        if (itemLevel > 0) {
            if (!canReadNextItem()) {
                return -2;
            }
        } else if (itemLevel == 0) {
            log.error("Item level is 0, so you can not check the next sub-item type.");
            return -1;
        }

        int type = 0;
        try {
            type = readLong() & 0x0000ffff;

            // reduce read length after reading as we will reset the stream
            readLength[itemLevel] -= 4;
        } catch (IOException e) {
            log.error("Error while checking the type of the subitem:\n" + e.getMessage());
        }
        try {
            dataStream.reset();
        } catch (IOException e) {
            log.error("Resetting data stream while checking the type of the subitem failed.\n"
                    + e.getMessage());
        }
        return type;
    }

    /**
     * Check if we are already beyond the top item and then if we will be beyond the next smaller
     * level (superior) item after reading this item's header.
     *
     * @return true if the end of the top level or next smaller level item is not reached, false
     * otherwise
     */
    public boolean canReadNextItem() {
        boolean topLevelEndReached =
                readLength[itemLevel] >= itemLength[0] + 16 + (itemExtension[0] ? 4 : 0);
        boolean subItemEndReached = readLength[itemLevel] + 12 >= itemLength[itemLevel - 1];
        return !(topLevelEndReached || subItemEndReached);
    }

    /**
     * Read the header of a sub-item, recognize the identification field of it and reset the stream
     * back.
     *
     * @return identification of a sub-item
     */
    public long nextSubitemIdent() {
        dataStream.mark(MAX_HEADER_SIZE);

        // Are we beyond the last sub-item?
        if (itemLevel > 0) {
            if (!canReadNextItem()) {
                return -2;
            }
        } else if (itemLevel == 0) {
            return -1;
        }

        int identification = 0;
        try {
            // read a long number containing the first part of header
            readLong();

            // read a long number containing the second part of header with the identification
            identification = readLong();

            // reduce read length after reading as we will reset the stream
            readLength[itemLevel] -= 8;

        } catch (IOException e) {
            log.error("Error while checking the type of the subitem:\n" + e.getMessage());
        }
        try {
            dataStream.reset();
        } catch (IOException e) {
            log.error("Resetting data stream while checking the type of the subitem failed.\n"
                    + e.getMessage());
        }
        return identification;
    }

    /**
     * Skip the sub-item if it is of no interest.
     */
    public boolean skipSubitem() {
        EventIOHeader header = new EventIOHeader(this);
        try {
            if (header.findAndReadNextHeader()) {
                return header.getItemEnd();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lookup from telescope ID to offset number (index) in structures.
     *
     * The lookup table must have been filled before with setTelIdx(). When dealing with multiple
     * lookups, use set_tel_idx_ref() first to select the lookup table to be used.
     *
     * @param telId A telescope ID for which we want the index count.
     * @return >= 0 (index in the original list passed to setTelIdx), -1 (not found in index, -2
     * (index not initialized).
     */
    public int findTelIndex(int telId) {
        if (gTelIdxInit[gTelIdxRef] == 0) {
            log.warn("Index was not initialized.");
            return -2;
        }
        if (telId < 0) {
            //TODO some memory "magic" is made in original code, can we skip it?
            log.warn("Negative ID was given.");
            return -1;
        }
        return gTelIdx[gTelIdxRef][telId];
    }

    /**
     * Setup of telescope index lookup table.
     *
     * Must be filled before first use of findTelIdx() - which is automatically done when reading a
     * run header data block. When dealing with multiple lookups, use set_tel_idx_ref() first to
     * select the one to fill.
     *
     * @param ntel The number of telescope following.
     * @param idx  The list of telescope IDs mapped to indices 0, 1, ...
     */
    public void setTelIdx(int ntel, int[] idx) {
        //TODO check if the java implementation is right
        for (int i = 0; i < H_MAX_TEL + 1; i++) {
            gTelIdx[gTelIdxRef][i] = -1;
        }
//        for (i = 0; (size_t) i < sizeof(gTelIdx[gTelIdxRef]) / sizeof(gTelIdx[gTelIdxRef][0]); i++) {
//            gTelIdx[gTelIdxRef][i] = -1;
//        }
        for (int i = 0; i < ntel; i++) {
            if (idx[i] < 0 || idx[i] >= H_MAX_TEL + 1) {
//            if (idx[i] < 0 || (size_t) idx[i] >=
//                    sizeof(gTelIdx[gTelIdxRef]) / sizeof(gTelIdx[gTelIdxRef][0])) {
                log.error("Telescope ID " + idx[i] + " is outside of valid range");
                return; //exit(1);
            }
            if (gTelIdx[gTelIdxRef][idx[i]] != -1) {
                log.error("Multiple telescope ID " + idx[i]
                        + "\nTelescope ID " + idx[i] + " is outside of valid range");
                return; //exit(1)
            }
            gTelIdx[gTelIdxRef][idx[i]] = i;
        }
        gTelIdxInit[gTelIdxRef] = 1;
    }

    /**
     * Read an byte from the stream as int.
     *
     * @return byte
     */
    public byte readByte() throws IOException {
        byte result = dataStream.readByte();
        readLength[itemLevel] += 1;
        return result;
    }

    /**
     * Read an unsigned byte from the stream as int.
     *
     * @return unsigned byte as short
     */
    public short readUnsignedByte() throws IOException {
        short result = (short) dataStream.readUnsignedByte();
        readLength[itemLevel] += 1;
        return result;
    }

    public short readShort() throws IOException {
        return readInt16();
    }

    /**
     * Read an unsigned short from an I/O buffer. The value is in the range of 0 to 65535.
     *
     * @return unsigned short
     */
    public int readUnsignedShort() throws IOException {
        //TODO check if filling up to an int with zeros is better than dataStream.readUnsignedShort()
        //TODO maybe use dataStream.readUnsignedByte()?
        byte[] b = new byte[4];
        if (EventIOStream.reverse) {
            dataStream.read(b, 0, 2);
        } else {
            dataStream.read(b, 2, 2);
        }

        readLength[itemLevel] += 2;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    /**
     * Read a 32-bit floating point number in IEEE format (4 bytes).
     *
     * @return float value that is read from readInt32()
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt32());
    }

    /**
     * Read a 64-bit floating point number in IEEE format (8 bytes)
     *
     * @return double value that is read from readInt64()
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readInt64());
    }

    public int readLong() throws IOException {
        return readInt32();
    }

    public short readInt16() throws IOException {
        byte[] b = new byte[2];
        dataStream.read(b);

        readLength[itemLevel] += 2;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } else {
            return ByteBuffer.wrap(b).getShort();
        }
    }

    public int readInt32() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        readLength[itemLevel] += 4;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    /**
     * Read an unsigned int from an I/O buffer.
     *
     * @return unsigned int as long
     */
    public long readUnsignedInt32() throws IOException {
        byte[] b = new byte[8];

        //TODO check if filling up to an int with zeros is better than dataStream.readUnsignedShort()
        //TODO maybe use dataStream.readUnsignedByte()?
        if (EventIOStream.reverse) {
            dataStream.read(b, 0, 4);
        } else {
            dataStream.read(b, 4, 4);
        }

        readLength[itemLevel] += 4;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            return ByteBuffer.wrap(b).getLong();
        }
    }

    public long readInt64() throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        readLength[itemLevel] += 8;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            return ByteBuffer.wrap(b).getLong();
        }
    }

    /**
     * Get a string of ASCII characters with leading count of bytes (stored with 16 bits) from an
     * I/O buffer.
     *
     * NOTE: the nmax count does now account for the trailing zero byte which will be appended. This
     * was different in an earlier version of this function where one additional byte had to be
     * available for the trailing zero byte.
     */

    public String readString(int nmax) throws IOException {
        int nbytes = readShort();
        int nread = (nmax - 1 < nbytes) ? nmax - 1 : nbytes; /* minimum of both */

        // Read up to the accepted maximum length
        char[] result = readVectorOfChars(nread);

        // Ignore the rest of the string
        if (nbytes > nread) {
            skipBytes(nbytes - nread);
        }

        // reduce the read length
        readLength[itemLevel] -= nbytes - nread;
        return String.valueOf(result);
    }

    /**
     * Calls internally readString(int nmax) with nmax=Short.MAX_VALUE to get the whole string
     * value. Get a string of ASCII characters with leading count of bytes (stored with 16 bits)
     * from an I/O buffer.
     */
    public String readString() throws IOException {
        return readString(Short.MAX_VALUE);
    }

    /**
     * Description from hessioxxx:
     *
     * @short Get an unsigned integer of unspecified length from an I/O buffer.
     *
     * Get an unsigned integer of unspecified length from an I/O buffer where it is encoded in a way
     * similar to the UTF-8 character encoding. Even though the scheme in principle allows for
     * arbitrary length data, the current implementation is limited for data of up to 64 bits. On
     * systems with @c uintmax_t shorter than 64 bits, the result could be clipped unnoticed. It
     * could also be clipped unnoticed in the application calling this function.
     */
    public long readCount() throws IOException {

        int countLength = 9;
//        long[] count = new long[countLength];
//
//        int[] masks = new int[]{0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff};
//
//        int length = 1;
//        if ((count[0] & masks[0]) == 0) {
//            calculateCount(count, length);
//        }
//
//        // TODO: control that this math works the same way as in hessioxxx
//        long result = 0;
//        for (int i = 1; i < countLength; i++) {
//            count[i] = readByte();
//            if ((count[0] & masks[i]) == masks[i - 1]) {
//                result = calculateCount(count, length);
//            }
//        }


        long[] v = new long[countLength];

        v[0] = readByte();

        if ((v[0] & 0x80) == 0) {
            return v[0];
        }
        v[1] = readByte();
        if ((v[0] & 0xc0) == 0x80) {
            return ((v[0] & 0x3f) << 8) | v[1];
        }
        v[2] = readByte();
        if ((v[0] & 0xe0) == 0xc0) {
            return ((v[0] & 0x1f) << 16) | (v[1] << 8) | v[2];
        }
        v[3] = readByte();
        if ((v[0] & 0xf0) == 0xe0) {
            return ((v[0] & 0x0f) << 24) | (v[1] << 16) | (v[2] << 8) | v[3];
        }
        v[4] = readByte();
        if ((v[0] & 0xf8) == 0xf0) {
            if ((v[0] & 0x07) != 0x00) {
                log.warn("Data too large in get_count32 function, clipped.");
            }
            return (v[1] << 24) | (v[2] << 16) | (v[3] << 8) | v[4];
        }
        // With only 32-bit integers available, we may lose data from here on.
        log.warn("Data too large in get_count32 function.");
        v[5] = readByte();
        if ((v[0] & 0xfc) == 0xf8) {
            return 0;
        }
        v[6] = readByte();
        if ((v[0] & 0xfe) == 0xfc) {
            return 0;
        }
        v[7] = readByte();
        if ((v[0] & 0xff) == 0xfe) {
            return 0;
        }
        v[8] = readByte();
        return 0;

//        count[0] = readByte();
//        if ((count[0] & masks[0]) == 0){
//            return count[0];
//        }
//
//        count[1] = readByte();
//        if ((count[0] & masks[1]) == masks[0]){
//            return ((count[0] & 0x3f) << 8 | count[1]);
//        }
//
//        count[2] = readByte();
//        if ((count[0] & masks[2]) == masks[1]){
//            return ((count[0] & 0x1f) << 16 | (count[1] << 8) | count[2]);
//        }

//        return result;
    }

    private long calculateCount(long[] count, int length) {
        int[] bitmasks = new int[]{0x3f, 0x1f, 0x0f, 0x07, 0x03, 0x01};

        long temp = count[length - 1];
        int shift = 0;

        for (int i = length - 2; i >= 1; i--) {
            temp |= (count[i] << shift);
            shift += 8;
        }

        if (length <= 6 && length > 1) {
            temp |= ((count[0] & bitmasks[length - 1]) & shift);
        }

        return temp;
    }

    /**
     * Get a signed integer of unspecified length from an I/O buffer where it is encoded in a way
     * similar to the UTF-8 character encoding. Even though the scheme in principle allows for
     * arbitrary length data, the current implementation is limited for data of up to 64 bits. On
     * systems with intmax_t shorter than 64 bits, the result could be clipped unnoticed.
     */
    public long readSCount() throws IOException {
        //TODO implement and think of 16, 32 and 64 variants
        long value = readCount();
        return unsignedToSignedCount(value);
    }

    public short readSCount16() throws IOException {
        int value = readCount16();
        return (short) unsignedToSignedCount(value);
    }

    public int readSCount32() throws IOException {
        long value = readCount32();
        return (int) unsignedToSignedCount(value);
    }

    /**
     * Transform unsigned count value to a signed one.
     */
    private long unsignedToSignedCount(long value) {
        //TODO is it the right implementation? do we get the lest significant bit at the right most position?
        // values of 0,1,2,3,4,... here correspond to signed values of
        // 0,-1,1,-2,2,... We have to test the least significant bit:
        if ((value & 1) == 1) {
            // Negative number
            return -(value >> 1) - 1;
        } else {
            return value >> 1;
        }
    }

    private int readCount16() {
        //TODO implement readcount16
        return 0;
    }

    private long readCount32() throws IOException {
        //TODO implement readcount32
        return readCount();
    }

    public float readSFloat() throws IOException {
        int shortFloat = readUnsignedShort();

        int sign = (shortFloat & 0x8000) >> 15;
        int exponent = (shortFloat & 0x7c00) >> 10;
        int mantissa = (shortFloat & 0x03ff);

        //TODO float or double?
        float s = (sign == 0) ? 1.f : -1.f;
        if (exponent == 0) /* De-normalized */ {
            if (mantissa == 0) {
                return s * 0.0f;
            } else {
                return s * mantissa / (1024 * 16384);
            }
        } else if (exponent < 31) {
            return s * ((float) Math.pow(2., exponent - 15.0)) * (1.f + mantissa / 1024.f);
        }

        //TODO do we need those IFs?
//        #ifdef INF
//        else if ( mantissa == 0 )
//            return s * INF;
//        #endif
        else {
//        #ifdef NAN
//        return NAN;
//        #else
            return 0.f;
        }
//        #endif
//        return 0;
    }

    /**
     * Get an array of ints as scount32 data from an I/O buffer.
     *
     * @param number number of ints to be read from buffer
     * @return array of ints
     */
    public int[] readVectorOfIntsScount(int number) throws IOException {
        int[] result = new int[number];
        for (int i = 0; i < number; i++) {
            result[i] = readSCount32();
        }
        return result;
    }

    /**
     * Read a vector of bytes from an I/O buffer.
     *
     * @param number number of elements to load
     * @return array of bytes
     */
    public byte[] readVectorOfBytes(int number) throws IOException {
        byte[] bytes = new byte[number];
        dataStream.read(bytes);
        readLength[itemLevel] += number;
        return bytes;
    }

    /**
     * Read a vector of chars from an I/O buffer.
     *
     * @param number number of elements to load
     * @return array of chars
     */
    public char[] readVectorOfChars(int number) throws IOException {
        char[] result = new char[number];
        for (int i = 0; i < number; i++) {
            result[i] = (char) readByte();
        }
        return result;
    }

    /**
     * Read a vector of unsigned bytes from an I/O buffer.
     *
     * @param number number of elements to load
     * @return array of shorts
     */
    public short[] readVectorOfUnsignedBytes(int number) throws IOException {
        short[] result = new short[number];
        for (int i = 0; i < number; i++) {
            result[i] = readUnsignedByte();
        }
        return result;
    }

    /**
     * Read a vector of unsigned shorts from an I/O buffer with least significant byte first. The
     * values are in the range 0 to 65535. The function should be used where sign propagation is of
     * concern.
     *
     * @param number number of elements to load
     * @return array of int elements
     */
    public int[] readVectorOfUnsignedShort(int number) throws IOException {
        int[] result = new int[number];
        for (int i = 0; i < number; i++) {
            result[i] = readUnsignedShort();
        }
        return result;
    }

    /**
     * Read a vector of shorts from an I/O buffer.
     *
     * @param number number of elements to load
     * @return array of int elements
     */
    public int[] readVectorOfShorts(int number) throws IOException {
        int[] result = new int[number];
        for (int i = 0; i < number; i++) {
            result[i] = readShort();
        }
        return result;
    }

    public float[] readVectorOfFloats(int number) throws IOException {
        float[] result = new float[number];
        for (int i = 0; i < number; i++) {
            result[i] = readFloat();
        }
        return result;
    }

    public double[] readVectorOfDoubles(int vectorSize)
            throws IOException {
        double[] vector = new double[vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            vector[i] = readDouble();
        }
        return vector;
    }
}
