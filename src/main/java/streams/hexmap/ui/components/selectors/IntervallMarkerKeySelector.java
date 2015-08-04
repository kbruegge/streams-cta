package streams.hexmap.ui.components.selectors;

import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.IntervallMarkerSelectionChangedEvent;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import streams.hexmap.ui.plotting.IntervalPlotData;
import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.PlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class IntervallMarkerKeySelector extends KeySelector {
    @Override
    public void selectionUpdate() {
        Bus.eventBus.post(new IntervallMarkerSelectionChangedEvent(getPlotData()));
    }

    @Override
    public Set<KeySelectorItem> filterItems(Data item) {
        Set<KeySelectorItem> newItems = new HashSet<>();
        for (String key : item.keySet()) {
            try {
                IntervalMarker[] i = (IntervalMarker[]) item.get(key);
                if(i.length == 1440) {
                    newItems.add(new KeySelectorItem(new IntervalPlotData(i, Color.GRAY, key), this));
                }
            } catch (ClassCastException e){
                continue;
            }

        }
        return newItems;
    }

    @Override
    public Set<IntervalPlotData> getPlotData() {
        return null;
    }
}
