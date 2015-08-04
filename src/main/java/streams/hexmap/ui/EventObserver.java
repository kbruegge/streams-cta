package streams.hexmap.ui;

import com.google.common.eventbus.Subscribe;
import streams.hexmap.ui.events.ItemChangedEvent;

/**
 * Created by kaibrugge on 29.04.14.
 */
public interface EventObserver {


    /**
     * Pass the event to this observer. The EventObserver has to decide what he wants to display.
     *
     * @param itemChangedEvent the current data item we want to display
     */
    @Subscribe
    public void handleEventChange(ItemChangedEvent itemChangedEvent);

}
