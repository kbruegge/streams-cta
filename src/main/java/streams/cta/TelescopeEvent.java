/**
 * 
 */
package streams.cta;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author chris
 * 
 */
public class TelescopeEvent implements Serializable {

	public final int numberOfPixels;
	public final short[][] data;
    public final int[] pixelIds;
    public final long eventId;
    public final LocalDateTime timeStamp;
    public final int roi;


    public TelescopeEvent(long eventId, int numberOfPixels, int[] pixelIds, short[][] data, LocalDateTime timeStamp) {
        this.numberOfPixels = numberOfPixels;
        this.data = data;
        this.pixelIds = pixelIds;
        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.roi = data[0].length;
    }
}
