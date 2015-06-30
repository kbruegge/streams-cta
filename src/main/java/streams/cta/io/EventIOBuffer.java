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

    DataInputStream dataStream;

    public EventIOBuffer(DataInputStream dataStream) {
        itemLength = new long[Constants.MAX_IO_ITEM_LEVEL];
        subItemLength = new long[Constants.MAX_IO_ITEM_LEVEL];
        itemStartOffset = new long[Constants.MAX_IO_ITEM_LEVEL];
        itemExtension = new boolean[Constants.MAX_IO_ITEM_LEVEL];
        this.dataStream = dataStream;
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
        return dataStream.readUnsignedByte();
    }

    //TODO: use float here?
    public double readReal() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return ByteBuffer.wrap(b).getFloat();
        }
    }

    public double readDouble() throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

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

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    public short readInt16() throws IOException {
        byte[] b = new byte[2];
        dataStream.read(b);

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } else {
            return ByteBuffer.wrap(b).getShort();
        }
    }

    public int readInt32() throws IOException {
        byte[] b = new byte[4];
        dataStream.read(b);

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
    }

    public long readInt64() throws IOException {
        byte[] b = new byte[8];
        dataStream.read(b);

        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            return ByteBuffer.wrap(b).getLong();
        }
    }

    public void skipBytes(int length) {
        try {
            dataStream.skipBytes(length);
        } catch (IOException e) {
            log.error("Skipping bytes produced an error:\n" + e.getMessage());
        }
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

}
