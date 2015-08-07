package streams.cta.io;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Breakdown of time into seconds since 1970.0 and nanoseconds.
 */
//TODO any better java structure for this purpose?!
public class HTime {
    long seconds;
    long nanoseconds;

    public HTime() {
        seconds = 0;
        nanoseconds = 0;
    }

    public void readTime(EventIOBuffer buffer) throws IOException {
        seconds = buffer.readLong();
        nanoseconds = buffer.readLong();
    }

    public void resetTime() {
        seconds = 0;
        nanoseconds = 0;
    }

    public LocalDateTime getAsLocalDateTime(){
        return LocalDateTime.ofEpochSecond(seconds, (int) nanoseconds, ZoneOffset.UTC);
    }
}
