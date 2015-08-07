package streams.hexmap.ui.events;

import stream.Data;
import streams.cta.CTATelescope;
import streams.hexmap.TelescopeEvent;

import java.time.LocalDateTime;

/**
 * This Event will be propagated to the UI in case a new DataItem arrives from the stream.
 *
 * Created by kai on 06.06.15.
 */
public class ItemChangedEvent {


    public final Data item;
    public final LocalDateTime timeStamp;
    public final CTATelescope telescope;
    public final short[][] rawData;
    public final int roi;

    public ItemChangedEvent(Data item, LocalDateTime timeStamp, CTATelescope telescope, short[][] rawData) {
        this.item = item;
        this.timeStamp = timeStamp;
        this.telescope = telescope;
        this.rawData = rawData;
        this.roi = rawData[0].length;
    }
}
