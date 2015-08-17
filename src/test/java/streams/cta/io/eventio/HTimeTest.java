package streams.cta.io.eventio;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import streams.cta.io.eventio.EventIOBuffer;
import streams.cta.io.eventio.EventIOStream;
import streams.cta.io.eventio.HTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static streams.cta.Constants.BIG_ENDIAN;

/**
 * Created by alexey on 07/08/15.
 */
public class HTimeTest {

    @Test
    public void testReadTime() throws Exception {
        // initialize data stream
        File f = new File("testInputStream.txt");
        DataOutputStream outDataStream = new DataOutputStream(new FileOutputStream(f));
        BufferedInputStream bStream = new BufferedInputStream(new FileInputStream(f), 1000);
        DataInputStream dataStream = new DataInputStream(bStream);

        // initialize buffer containing the data stream to read from it
        EventIOBuffer buffer = new EventIOBuffer(dataStream);

        EventIOStream.byteOrder = BIG_ENDIAN;

        int seconds = 12345;
        int nanoseconds = 67890;
        outDataStream.writeInt(seconds);
        outDataStream.writeInt(nanoseconds);

        HTime time = new HTime();
        time.readTime(buffer);

        assertEquals(seconds, time.seconds);
        assertEquals(nanoseconds, time.nanoseconds);

        if (f.delete()) {
            System.out.println("Deleting test file for JUnit tests was not successful.");
        }
    }

    @Test
    public void testResetTime() throws Exception {
        HTime time = new HTime();
        time.seconds = 1;
        time.nanoseconds = 2;
        assertEquals(1, time.seconds);
        assertEquals(2, time.nanoseconds);

        time.resetTime();

        assertNotSame(1, time.seconds);
        assertNotSame(2, time.nanoseconds);
    }

    @Test
    public void testGetAsLocalDateTime() throws Exception {
        HTime time = new HTime();
        int seconds = 12345;
        int nanoseconds = 67890;
        time.seconds = seconds;
        time.nanoseconds = nanoseconds;

        LocalDateTime ldt = LocalDateTime.ofEpochSecond(seconds, nanoseconds, ZoneOffset.UTC);
        assertEquals(ldt, time.getAsLocalDateTime());
    }
}