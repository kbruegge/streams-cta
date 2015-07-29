/**
 * 
 */
package streams.cta.container;

import streams.cta.CTATelescope;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class contains the raw data from a single telescope.
 * @author Kai
 */
public class EventData implements Serializable {
	public final short[][] data;
    public final int roi;


    public EventData(long eventId, short[][] data) {
        this.data = data;
        this.roi = data[0].length;
    }
}
