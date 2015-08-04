package streams.cta.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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
        byte[] b = new byte[]{1, 2, 3, 4};
        outDataStream.write(b);
        outDataStream.close();
        assertEquals(buffer.readByte(), b[0]);
        buffer.skipBytes(2);
        assertEquals(buffer.readByte(), b[3]);
    }

    @Test
    public void testReadUnsignedByte() throws Exception {

    }

    @Test
    public void testReadBytes() throws Exception {
        byte[] b = new byte[]{1, 2, 3, 4};
        outDataStream.write(b);
        outDataStream.close();
        byte[] readB = buffer.readBytes(4);
        for (int i = 0; i < readB.length; i++) {
            assertEquals(readB[i], b[i]);
        }
    }

    @Test
    public void testReadShort() throws Exception {

        short toread = (short) 0xAABB;
        
        // check BigEndian
        EventIOStream.reverse = false;
        short towrite = (short) 0xAABB;
        outDataStream.writeShort(towrite);
        outDataStream.flush();
        int readShort = buffer.readShort();
        assertEquals(toread, readShort);

        // check LittleEndian
        EventIOStream.reverse = true;
        towrite = (short) 0xBBAA;
        outDataStream.writeShort(towrite);
        outDataStream.flush();
        readShort = buffer.readShort();
        assertEquals(toread, readShort);
    }

    @Test
    public void testReadUnsignedShort() throws Exception {

        // check BigEndian
        EventIOStream.reverse = false;
        outDataStream.writeShort(0xFFFE);
        outDataStream.flush();
        int readShort = buffer.readUnsignedShort();
        assertEquals(0xFFFE, readShort);

        // check LittleEndian
        EventIOStream.reverse = true;
        outDataStream.writeShort(0xFEFF);
        outDataStream.close();
        readShort = buffer.readUnsignedShort();
        assertEquals(0xFFFE, readShort);
    }

    @Test
    public void testReadReal() throws Exception {

    }

    @Test
    public void testReadDouble() throws Exception {

    }

    @Test
    public void testReadLong() throws Exception {

    }

    @Test
    public void testReadInt16() throws Exception {

    }

    @Test
    public void testReadInt32() throws Exception {

    }

    @Test
    public void testReadUnsignedInt32() throws Exception {

    }

    @Test
    public void testReadInt64() throws Exception {

    }

    @Test
    public void testReadString() throws Exception {

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

    }

    @Test
    public void testReadVectorOfChars() throws Exception {

    }

    @Test
    public void testReadVectorOfUnsignedBytes() throws Exception {

    }

    @Test
    public void testReadVectorOfUnsignedShort() throws Exception {

    }

    @Test
    public void testReadVectorOfInts() throws Exception {

    }

    @Test
    public void testReadVectorOfFloats() throws Exception {

    }

    @Test
    public void testReadVectorOfReals() throws Exception {

    }
}