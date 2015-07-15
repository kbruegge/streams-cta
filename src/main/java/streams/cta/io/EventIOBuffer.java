package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import streams.cta.Constants;

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
    int readLength;

    static int[][] gTelIdx = new int[3][Constants.H_MAX_TEL + 1];
    static int[] gTelIdxInit = new int[3];

    /**
     * Reference number when dealing with multiple telescope lookup tables.
     */
    static int gTelIdxRef = 0;

    DataInputStream dataStream;

    public EventIOBuffer(DataInputStream dataStream) {
        itemLength = new long[Constants.MAX_IO_ITEM_LEVEL];
        subItemLength = new long[Constants.MAX_IO_ITEM_LEVEL];
        itemStartOffset = new long[Constants.MAX_IO_ITEM_LEVEL];
        itemExtension = new boolean[Constants.MAX_IO_ITEM_LEVEL];
        this.dataStream = dataStream;
        readLength = 0;
    }

    public double[] readVectorOfReals(int vectorSize)
            throws IOException {
        double[] vector = new double[vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            vector[i] = readReal();
        }
        return vector;
    }

    /**
     * Read an unsigned byte from the stream as int.
     *
     * @return unsigned byte as int
     */
    public int readByte() throws IOException {
        int result = dataStream.readUnsignedByte();
        readLength += 1;
        return result;
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        dataStream.read(bytes);
        readLength += length;
        return bytes;
    }

    //TODO: use float here?
    public float readReal() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        readLength += 4;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return ByteBuffer.wrap(b).getFloat();
        }
    }

    public double readDouble() throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        readLength += 8;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        } else {
            return ByteBuffer.wrap(b).getDouble();
        }
    }

    //TODO check conversion from int to long?!
    public int readLong() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        readLength += 4;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    //TODO is it the supposed way to read a short?
    public short readShort() throws IOException {
        return readInt16();
    }

    public short readInt16() throws IOException {
        byte[] b = new byte[2];
        dataStream.read(b);

        readLength += 2;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } else {
            return ByteBuffer.wrap(b).getShort();
        }
    }

    public int readInt32() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        readLength += 4;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    public long readInt64() throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        readLength += 8;

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            return ByteBuffer.wrap(b).getLong();
        }
    }

    public void skipBytes(int length) {
        try {
            dataStream.skipBytes(length);
            readLength += length;
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
        //TODO use a constant for a maximum header length
        dataStream.mark(100);

        // Are we beyond the last sub-item?
        if (itemLevel > 0) {
            // First check if we are already beyond the top item and then if we
            // will be beyond the next smaller level (superiour) item after
            // reading this item's header.
            // TODO do the check as in eventio.c, line 3454
        } else if (itemLevel == 0) {
            return -1;
        }

        int type = 0;
        try {
            type = readLong() & 0x0000ffff;
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
     * Read the header of a sub-item, recognize the identification field of it and reset the stream
     * back.
     *
     * @return identification of a sub-item
     */
    public long nextSubitemIdent() {
        //TODO use a constant for a maximum header length
        dataStream.mark(100);

        // Are we beyond the last sub-item?
        if (itemLevel > 0) {
            // First check if we are already beyond the top item and then if we
            // will be beyond the next smaller level (superiour) item after
            // reading this item's header.
            // TODO do the check as in eventio.c, line 3454
        } else if (itemLevel == 0) {
            return -1;
        }

        int identification = 0;
        try {
            // read a long number containing the first part of header
            readLong();

            // read a long number containing the second part of header with the identification
            identification = readLong();

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
        long[] count = new long[countLength];

        int[] masks = new int[]{0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff};

        int length = 1;
        if ((count[0] & masks[0]) == 0) {
            calculateCount(count, length);
        }

        // TODO: control that this math works the same way as in hessioxxx
        long result = 0;
        for (int i = 1; i < countLength; i++) {
            count[i] = readByte();
            if ((count[0] & masks[i]) == masks[i - 1]) {
                result = calculateCount(count, length);
            }
        }

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

        return result;
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
     * Read a vector of unsigned shorts from an I/O buffer with least significant byte first. The
     * values are in the range 0 to 65535. The function should be used where sign propagation is of
     * concern.
     *
     * @param number number of elements to load
     * @return array of elements
     */
    public int[] readVectorOfUnsignedShort(int number) throws IOException {
        int[] result = new int[number];
        for (int i = 0; i < number; i++) {
            result[i] = readUnsignedShort();
        }
        return result;
    }

    public short[] readVectorOfUnsignedBytes(int number) throws IOException {
        short[] result = new short[number];
        for (int i = 0; i < number; i++) {
            result[i] = (short) dataStream.readUnsignedByte();
        }
        return result;
    }

    /**
     * Read an unsigned short from an I/O buffer. The value is in the range of 0 to 65535.
     *
     * @return unsigned short
     */
    public int readUnsignedShort() throws IOException {
        return dataStream.readUnsignedShort();
    }

    public int[] readVectorOfInts(int number) throws IOException {
        int[] result = new int[number];
        for (int i = 0; i < number; i++) {
            result[i] = readShort();
        }
        return result;
    }

    public float[] readVectorOfFloats(int number) throws IOException {
        float[] result = new float[number];
        for (int i = 0; i < number; i++) {
            result[i] = (float) readReal();
        }
        return result;
    }

    /**
     * Lookup from telescope ID to offset number (index) in structures.
     *
     * The lookup table must have been filled before with set_tel_idx(). When dealing with multiple
     * lookups, use set_tel_idx_ref() first to select the lookup table to be used.
     *
     * @param telId A telescope ID for which we want the index count.
     * @return >= 0 (index in the original list passed to set_tel_idx), -1 (not found in index, -2
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
     * Skip the sub-item if it is of no interest.
     */
    public boolean skipSubitem() {
        EventIOHeader header = new EventIOHeader(this);
        try {
            if (header.findAndReadNextHeader()) {
                header.getItemEnd();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean readShower() {
        //TODO implement and move to another class
        return false;
    }

    public int readSCount() {
        //TODO implement and think of 16, 32 and 64 variants
        return -1;
    }

    public int[] readVectorOfIntsScount(int pixels) {
        //TODO implement
        return new int[0];
    }
}
