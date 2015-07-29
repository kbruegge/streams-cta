package streams.hexmap.ui.events;

import stream.Data;
import streams.cta.CTATelescope;
import streams.cta.container.EventData;

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
    public final EventData eventData;

    public ItemChangedEvent(Data item, LocalDateTime timeStamp, CTATelescope telescope, EventData eventData) {
        this.item = item;
        this.timeStamp = timeStamp;
        this.telescope = telescope;
        this.eventData = eventData;
    }
}
