package streams.cta.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import static streams.cta.Constants.LITTLE_ENDIAN;
import static streams.cta.Constants.BIG_ENDIAN;

/**
 * Created by alexey on 04/08/15.
 */
public class EventIOBufferTest {

    EventIOBuffer buffer;
    DataOutputStream outDataStream;
    File f;

    @Before
    public void setUp() throws Exception {

        // initialize data stream
        f = new File("testInputStream.txt");
        outDataStream = new DataOutputStream(new FileOutputStream(f));
        BufferedInputStream bStream = new BufferedInputStream(new FileInputStream(f), 1000);
        DataInputStream dataStream = new DataInputStream(bStream);

        // initialize buffer containing the data stream to read from it
        buffer = new EventIOBuffer(dataStream);

        EventIOStream.byteOrder = BIG_ENDIAN;
    }

    @After
    public void tearDown() throws Exception {
        if (!f.delete()) {
            System.out.println("ERROR: test input stream file could not have been deleted.");
        }
    }

    @Test
    public void testSkipBytes() throws Exception {
        byte[] b = new byte[]{1, 2, 3, 4};
        outDataStream.write(b);
        outDataStream.close();
        assertEquals(buffer.dataStream.available(), b.length);
        buffer.skipBytes(2);
        assertEquals(buffer.dataStream.available(), b.length - 2);
    }

    @Test
    public void testNextSubitemType() throws Exception {
        int type = 0x0011;
        int version = 0x01010000;
        outDataStream.writeInt(type | version);
        outDataStream.flush();

        // make it possible to check the next subitem
        buffer.itemLevel = 1;
        buffer.itemLength[0] = 1024;
        buffer.itemLength[1] = 1024;
        long readType = buffer.nextSubitemType();
        assertEquals(type, readType);
    }

    @Test
    public void testNextSubitemIdent() throws Exception {
        int type = 0x0011;
        outDataStream.writeInt(type);
        int ident = 0x1111;
        outDataStream.writeInt(ident);
        outDataStream.writeChars("Something more after the ident.");
        outDataStream.flush();

        // make it possible to check the next subitem
        buffer.itemLevel = 1;
        buffer.itemLength[0] = 1024;
        buffer.itemLength[1] = 1024;
        long readIdent = buffer.nextSubitemIdent();
        assertEquals(ident, readIdent);
        assertNotSame(type, readIdent);
    }

    @Test
    public void testReadByte() throws Exception {
        outDataStream.writeByte(12);
        outDataStream.close();
        assertEquals(12, buffer.readByte());
    }

    @Test
    public void testReadUnsignedByte() throws Exception {
        short toread = 0xF;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        outDataStream.writeByte(toread);
        outDataStream.flush();
        short readShort = buffer.readUnsignedByte();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        outDataStream.writeByte(toread);
        outDataStream.close();
        readShort = buffer.readUnsignedByte();
        assertEquals(toread, readShort);
    }

    @Test
    /**
     * ReadShort uses readInt16 internally, thus no further tests are called for readInt16
     */
    public void testReadShort() throws Exception {
        short toread = (short) 0xFFFF;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        outDataStream.writeShort(toread);
        outDataStream.flush();
        int readShort = buffer.readShort();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        outDataStream.writeShort(Short.reverseBytes(toread));
        outDataStream.flush();
        readShort = buffer.readShort();
        assertEquals(toread, readShort);
    }

    @Test
    public void testReadUnsignedShort() throws Exception {

        int toread = 0x0000FFFF;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        outDataStream.writeShort((short) toread);
        outDataStream.flush();
        int readShort = buffer.readUnsignedShort();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        outDataStream.writeShort(Short.reverseBytes((short) toread));
        outDataStream.close();
        readShort = buffer.readUnsignedShort();
        assertEquals(toread, readShort);
    }

    @Test
    public void testReadReal() throws Exception {

        float toread = 1.171539f;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        byte[] b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(toread).array();
        outDataStream.write(b);
        outDataStream.flush();
        float readReal = buffer.readFloat();
        assertEquals(toread, readReal, 0.1);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(toread).array();
        outDataStream.write(b);
        outDataStream.close();
        readReal = buffer.readFloat();
        assertEquals(toread, readReal, 0.1);
    }

    @Test
    public void testReadDouble() throws Exception {
        double toread = 1.171539d;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        byte[] b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putDouble(toread).array();
        outDataStream.write(b);
        outDataStream.flush();
        double readReal = buffer.readDouble();
        assertEquals(toread, readReal, 0.1);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        b = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(toread).array();
        outDataStream.write(b);
        outDataStream.close();
        readReal = buffer.readDouble();
        assertEquals(toread, readReal, 0.1);
    }

    @Test
    public void testReadInt32() throws Exception {
        int toread = 0xAABBCCDD;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        int towrite = toread;
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        int readInt32 = buffer.readInt32();
        assertEquals(toread, readInt32);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        towrite = Integer.reverseBytes(toread);
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        readInt32 = buffer.readInt32();
        assertEquals(toread, readInt32);
    }

    @Test
    public void testReadUnsignedInt32() throws Exception {
        long toread = 0x00000000AABBCCDDL;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        int towrite = (int) toread;
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        long readInt32 = buffer.readUnsignedInt32();
        assertEquals(toread, readInt32);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        towrite = Integer.reverseBytes(towrite);
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        readInt32 = buffer.readUnsignedInt32();
        assertEquals(toread, readInt32);
    }

    @Test
    public void testReadInt64() throws Exception {
        long toread = 0x00000000AABBCCDDL;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        outDataStream.writeLong(toread);
        outDataStream.flush();
        long readInt64 = buffer.readInt64();
        assertEquals(toread, readInt64);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        outDataStream.writeLong(Long.reverseBytes(toread));
        outDataStream.flush();
        readInt64 = buffer.readInt64();
        assertEquals(toread, readInt64);
    }

    @Test
    public void testReadString() throws Exception {
        String toread = "This is a test for reading string value.";
        short toreadLength = (short) toread.length();

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        outDataStream.writeShort(toreadLength);
        outDataStream.write(toread.getBytes());
        outDataStream.flush();
        String readString = buffer.readString();
        assertEquals(0, buffer.dataStream.available());
        assertEquals(toread, readString);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        outDataStream.writeShort(Short.reverseBytes(toreadLength));
        outDataStream.write(toread.getBytes());
        outDataStream.flush();
        readString = buffer.readString();
        assertEquals(0, buffer.dataStream.available());
        assertEquals(toread, readString);
    }

    @Test
    public void testReadCount() throws Exception {
        byte b = 0x7F;
        outDataStream.writeByte(b);
        outDataStream.flush();
        assertEquals(b, buffer.readCount());

        short s = 0x81;
        writeCount(s);
        outDataStream.flush();
        assertEquals(s, buffer.readCount());

        int i = 0x7881;
        writeCount(i);
        outDataStream.flush();
        assertEquals(i, buffer.readCount());

        int li = 0xFF7881;
        writeCount(li);
        outDataStream.flush();
        assertEquals(li, buffer.readCount());

        long vli = 0xFFF7881;
        writeCount(vli);
        outDataStream.flush();
        assertEquals(vli, buffer.readCount());

        long vvli = 0x3FFF7881;
        writeCount(vvli);
        outDataStream.flush();
        assertEquals(vvli, buffer.readCount());

        vvli = 0x5D591281;
        writeCount(vvli);
        outDataStream.flush();
        assertEquals(vvli, buffer.readCount());
    }

    private void writeCount(long n) {
        long[] v = new long[9]; /* Prepared for up to 64 bits */
        int one = 1;
        int l;
        if (n < (one << 7)) {
            v[0] = (byte) n;
            l = 1;
        } else if (n < (one << 14)) {
            v[0] = 0x80 | ((n >> 8) & 0x3f);
            v[1] = (n & 0xff);
            l = 2;
        } else if (n < (one << 21)) {
            v[0] = 0xc0 | ((n >> 16) & 0x1f);
            v[1] = ((n >> 8) & 0xff);
            v[2] = (n & 0xff);
            l = 3;
        } else if (n < (one << 28)) {
            v[0] = 0xe0 | ((n >> 24) & 0x0f);
            v[1] = ((n >> 16) & 0xff);
            v[2] = ((n >> 8) & 0xff);
            v[3] = (n & 0xff);
            l = 4;
        } else if (n < (one << 35)) /* possible for 64-bit integers */ {
            v[0] = 0xf0 | ((n >> 32) & 0x07);
            v[1] = ((n >> 24) & 0xff);
            v[2] = ((n >> 16) & 0xff);
            v[3] = ((n >> 8) & 0xff);
            v[4] = (n & 0xff);
            l = 5;
        } else if (n < (one << 42)) {
            v[0] = 0xf8 | ((n >> 40) & 0x03);
            v[1] = ((n >> 32) & 0xff);
            v[2] = ((n >> 24) & 0xff);
            v[3] = ((n >> 16) & 0xff);
            v[4] = ((n >> 8) & 0xff);
            v[5] = (n & 0xff);
            l = 6;
        } else if (n < (one << 49)) {
            v[0] = 0xfc | ((n >> 48) & 0x01);
            v[1] = ((n >> 40) & 0xff);
            v[2] = ((n >> 32) & 0xff);
            v[3] = ((n >> 24) & 0xff);
            v[4] = ((n >> 16) & 0xff);
            v[5] = ((n >> 8) & 0xff);
            v[6] = (n & 0xff);
            l = 7;
        } else if (n < (one << 56)) {
            v[0] = 0xfe;
            v[1] = ((n >> 48) & 0xff);
            v[2] = ((n >> 40) & 0xff);
            v[3] = ((n >> 32) & 0xff);
            v[4] = ((n >> 24) & 0xff);
            v[5] = ((n >> 16) & 0xff);
            v[6] = ((n >> 8) & 0xff);
            v[7] = (n & 0xff);
            l = 8;
        } else /* For n < 2^63 strictly but as long as we have no plans for */ {    /* including numbers >= 2^64, it works up to 2^64-1. */
            v[0] = 0xff;
            v[1] = ((n >> 56) & 0xff);
            v[2] = ((n >> 48) & 0xff);
            v[3] = ((n >> 40) & 0xff);
            v[4] = ((n >> 32) & 0xff);
            v[5] = ((n >> 24) & 0xff);
            v[6] = ((n >> 16) & 0xff);
            v[7] = ((n >> 8) & 0xff);
            v[8] = (n & 0xff);
            l = 9;
        }

        for (int i = 0; i < l; i++) {
            try {
                outDataStream.writeByte((byte)v[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testReadSCount() throws Exception {

    }

    @Test
    public void testReadSFloat() throws Exception {

    }

    @Test
    public void testReadVectorOfBytes() throws Exception {
        byte[] b = new byte[]{1, 2, 3, 4};
        outDataStream.write(b);
        outDataStream.close();
        assertArrayEquals(b, buffer.readVectorOfBytes(4));
    }

    @Test
    public void testReadVectorOfChars() throws Exception {
        String toread = "test";
        outDataStream.write(toread.getBytes());
        outDataStream.close();
        assertArrayEquals(toread.toCharArray(), buffer.readVectorOfChars(toread.length()));
    }

    @Test
    public void testReadVectorOfUnsignedBytes() throws Exception {
        byte[] toread = {0xF, 0x2, 0xA, 0x5};
        outDataStream.write(toread);
        outDataStream.flush();
        short[] readShort = buffer.readVectorOfUnsignedBytes(4);
        for (int i = 0; i < 4; i++) {
            assertEquals(toread[i], readShort[i]);
        }
    }

    @Test
    public void testReadVectorOfUnsignedShort() throws Exception {
        int[] toread = {0xFFFF, 0x2222, 0xAAAA, 0x2315};
        int toreadLength = toread.length;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        for (int towrite : toread) {
            outDataStream.writeShort((short) towrite);
        }
        outDataStream.flush();
        int[] readShort = buffer.readVectorOfUnsignedShort(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readShort[i]);
        }

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        for (int towrite : toread) {
            outDataStream.writeShort(Short.reverseBytes((short) towrite));
        }
        outDataStream.close();
        readShort = buffer.readVectorOfUnsignedShort(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readShort[i]);
        }
    }

    @Test
    public void testReadVectorOfShort() throws Exception {
        short[] toread = {0x1FFF, 0x2222, 0x21AA, 0x2315};
        int toreadLength = toread.length;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        for (short towrite : toread) {
            outDataStream.writeShort(towrite);
        }
        outDataStream.flush();
        short[] readVectorOfInts = buffer.readVectorOfShorts(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readVectorOfInts[i]);
        }

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        for (short towrite : toread) {
            outDataStream.writeShort(Short.reverseBytes(towrite));
        }
        outDataStream.close();
        readVectorOfInts = buffer.readVectorOfShorts(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readVectorOfInts[i]);
        }
    }

    @Test
    public void testReadVectorOfFloats() throws Exception {
        float[] toread = {1.171539f, 2.1234f, -30.432f, -100.3f};
        int toreadLength = toread.length;

        // check BigEndian
        EventIOStream.byteOrder = BIG_ENDIAN;
        for (float towrite : toread) {
            byte[] b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(towrite).array();
            outDataStream.write(b);
        }
        outDataStream.flush();
        float[] readVectorOfFloats = buffer.readVectorOfFloats(toreadLength);
        assertArrayEquals(toread, readVectorOfFloats, 0.1f);

        // check LittleEndian
        EventIOStream.byteOrder = LITTLE_ENDIAN;
        for (float towrite : toread) {
            byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(towrite).array();
            outDataStream.write(b);
        }
        outDataStream.close();
        readVectorOfFloats = buffer.readVectorOfFloats(toreadLength);
        assertArrayEquals(toread, readVectorOfFloats, 0.1f);
    }
}