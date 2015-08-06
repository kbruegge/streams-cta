package streams.cta.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        FileInputStream bStream = new FileInputStream(f);
        DataInputStream dataStream = new DataInputStream(bStream);

        // initialize buffer containing the data stream to read from it
        buffer = new EventIOBuffer(dataStream);

        EventIOStream.reverse = false;
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

    }

    @Test
    public void testNextSubitemIdent() throws Exception {

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
        EventIOStream.reverse = false;
        outDataStream.writeByte(toread);
        outDataStream.flush();
        short readShort = buffer.readUnsignedByte();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        outDataStream.writeShort(toread);
        outDataStream.flush();
        int readShort = buffer.readShort();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.reverse = true;
        outDataStream.writeShort(Short.reverseBytes(toread));
        outDataStream.flush();
        readShort = buffer.readShort();
        assertEquals(toread, readShort);
    }

    @Test
    public void testReadUnsignedShort() throws Exception {

        int toread = 0x0000FFFF;

        // check BigEndian
        EventIOStream.reverse = false;
        outDataStream.writeShort((short) toread);
        outDataStream.flush();
        int readShort = buffer.readUnsignedShort();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.reverse = true;
        outDataStream.writeShort(Short.reverseBytes((short) toread));
        outDataStream.close();
        readShort = buffer.readUnsignedShort();
        assertEquals(toread, readShort);
    }

    @Test
    public void testReadReal() throws Exception {

        float toread = 1.171539f;

        // check BigEndian
        EventIOStream.reverse = false;
        byte[] b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(toread).array();
        outDataStream.write(b);
        outDataStream.flush();
        float readReal = buffer.readReal();
        assertEquals(toread, readReal, 0.1);

        // check LittleEndian
        EventIOStream.reverse = true;
        b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(toread).array();
        outDataStream.write(b);
        outDataStream.close();
        readReal = buffer.readReal();
        assertEquals(toread, readReal, 0.1);
    }

    @Test
    public void testReadDouble() throws Exception {
        double toread = 1.171539d;

        // check BigEndian
        EventIOStream.reverse = false;
        byte[] b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putDouble(toread).array();
        outDataStream.write(b);
        outDataStream.flush();
        double readReal = buffer.readDouble();
        assertEquals(toread, readReal, 0.1);

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        int towrite = toread;
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        int readInt32 = buffer.readInt32();
        assertEquals(toread, readInt32);

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        int towrite = (int) toread;
        outDataStream.writeInt(towrite);
        outDataStream.flush();
        long readInt32 = buffer.readUnsignedInt32();
        assertEquals(toread, readInt32);

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        outDataStream.writeLong(toread);
        outDataStream.flush();
        long readInt64 = buffer.readInt64();
        assertEquals(toread, readInt64);

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        outDataStream.writeShort(toreadLength);
        outDataStream.write(toread.getBytes());
        outDataStream.flush();
        String readString = buffer.readString();
        assertEquals(0, buffer.dataStream.available());
        assertEquals(toread, readString);

        // check LittleEndian
        EventIOStream.reverse = true;
        outDataStream.writeShort(Short.reverseBytes(toreadLength));
        outDataStream.write(toread.getBytes());
        outDataStream.flush();
        readString = buffer.readString();
        assertEquals(0, buffer.dataStream.available());
        assertEquals(toread, readString);
    }

    @Test
    public void testReadCount() throws Exception {

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
        EventIOStream.reverse = false;
        for (int towrite : toread) {
            outDataStream.writeShort((short) towrite);
        }
        outDataStream.flush();
        int[] readShort = buffer.readVectorOfUnsignedShort(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readShort[i]);
        }

        // check LittleEndian
        EventIOStream.reverse = true;
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
        EventIOStream.reverse = false;
        for (short towrite : toread) {
            outDataStream.writeShort(towrite);
        }
        outDataStream.flush();
        int[] readVectorOfInts = buffer.readVectorOfShort(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readVectorOfInts[i]);
        }

        // check LittleEndian
        EventIOStream.reverse = true;
        for (short towrite : toread) {
            outDataStream.writeShort(Short.reverseBytes(towrite));
        }
        outDataStream.close();
        readVectorOfInts = buffer.readVectorOfShort(toreadLength);
        for (int i = 0; i < toreadLength; i++) {
            assertEquals(toread[i], readVectorOfInts[i]);
        }
    }

    @Test
    public void testReadVectorOfFloats() throws Exception {
        float[] toread = {1.171539f, 2.1234f, -30.432f, -100.3f};
        int toreadLength = toread.length;

        // check BigEndian
        EventIOStream.reverse = false;
        for (float towrite : toread) {
            byte[] b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(towrite).array();
            outDataStream.write(b);
        }
        outDataStream.flush();
        float[] readVectorOfFloats = buffer.readVectorOfFloats(toreadLength);
        assertArrayEquals(toread, readVectorOfFloats, 0.1f);

        // check LittleEndian
        EventIOStream.reverse = true;
        for (float towrite : toread) {
            byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(towrite).array();
            outDataStream.write(b);
        }
        outDataStream.close();
        readVectorOfFloats = buffer.readVectorOfFloats(toreadLength);
        assertArrayEquals(toread, readVectorOfFloats, 0.1f);
    }
}