package streams.cta.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import streams.cta.Constants;

/**
 * Created by alexey on 17.06.15.
 */
public class EventIOBuffer {

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
     * Calculate integer value of a byte array with a length of 4
     *
     * @param b byte array
     * @return integer value of a byte array
     */
    public int byteArrayToInt(byte[] b) {
        if (b.length != 4) {
            // TODO throw exception if this should happen?
            return 0;
        }
        if (EventIOStream.reverse) {
            return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return ByteBuffer.wrap(b).getInt();
        }
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
}
