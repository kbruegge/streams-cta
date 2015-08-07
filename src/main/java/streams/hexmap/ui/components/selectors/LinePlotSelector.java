package streams.hexmap.ui.components.selectors;

import streams.cta.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.PlotSelectionChangedEvent;
import stream.Data;
import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.PlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 21.07.15.
 */
public class LinePlotSelector extends KeySelector {




    @Override
    public Set<KeySelectorItem> filterItems(Data item, TelescopeEvent telescopeEvent) {
        Set<KeySelectorItem> newItems = new HashSet<>();
        for  (String key: item.keySet()){
            try {
                double[][] data = (double[][]) item.get(key);
                if(data.length == telescopeEvent.numberOfPixel) {
                    newItems.add(new KeySelectorItem(new LinePlotData(data, Color.GRAY, key), this));
                }
            } catch (ClassCastException e){
//                Do nothing and continue loop.
            }
        }

        double[][] data = new double[telescopeEvent.numberOfPixel][telescopeEvent.roi];
        for (int pixel = 0; pixel < telescopeEvent.numberOfPixel; pixel++) {
            for (int slice = 0; slice < telescopeEvent.roi; slice++) {
                data[pixel][slice] = telescopeEvent.data[pixel][slice];
            }
        }

        newItems.add(new KeySelectorItem(new LinePlotData(data, Color.LIGHT_GRAY, "@event"), this));
        return newItems;
    }


}
