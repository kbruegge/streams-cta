package streams.hexmap.ui.components.selectors;

import streams.cta.TelescopeEvent;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import streams.hexmap.ui.plotting.IntervalPlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class IntervallMarkerKeySelector extends KeySelector {

    @Override
    public Set<KeySelectorItem> filterItems(Data item, TelescopeEvent telescopeEvent) {
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

}
