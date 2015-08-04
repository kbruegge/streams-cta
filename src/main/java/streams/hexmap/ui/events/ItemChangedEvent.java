package streams.hexmap.ui.events;

import stream.Data;
import streams.cta.TelescopeEvent;

/**
 * This Event will be propagated to the UI in case a new DataItem arrives from the stream.
 *
 * Created by kai on 06.06.15.
 */
public class ItemChangedEvent {
    public final Data item;
    public final TelescopeEvent telescopeEvent;


    public ItemChangedEvent(Data item, TelescopeEvent telescopeEvent) {
        this.item = item;
        this.telescopeEvent = telescopeEvent;
    }
}
