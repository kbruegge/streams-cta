package streams.hexmap.ui.components.selectors;

import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.IntervallMarkerSelectionChangedEvent;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class IntervallMarkerKeySelector extends KeySelector {
    @Override
    public void selectionUpdate() {
        Bus.eventBus.post(new IntervallMarkerSelectionChangedEvent(getSelectedItemPairs()));
    }

    @Override
    public Set<SeriesKeySelectorItem> filterItems(Data item) {
        Set<SeriesKeySelectorItem> newItems = new HashSet<>();
        for (String key : item.keySet()) {
            try {
                IntervalMarker[] i = (IntervalMarker[]) item.get(key);
                if(i.length == 1440) {
                    newItems.add(new SeriesKeySelectorItem(key, Color.LIGHT_GRAY, this));
                }
            } catch (ClassCastException e){
                continue;
            }

        }
        return newItems;
    }
}