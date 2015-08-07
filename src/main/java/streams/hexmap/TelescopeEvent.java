package streams.hexmap;


import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author kai
 *
 */
public class TelescopeEvent implements Serializable {
    public final int numberOfPixel = 1800;
    public final int triggerType = 0;
    public final short[][] data;
    public final long eventId;
    public final LocalDateTime timeStamp;
    public final int roi;

    public TelescopeEvent(long eventId, short[][] data, LocalDateTime timeStamp) {
        this.data = data;
        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.roi = data[0].length;
    }


}