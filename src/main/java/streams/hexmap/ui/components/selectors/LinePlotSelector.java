package streams.hexmap.ui.components.selectors;

import streams.hexmap.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.ItemChangedEvent;
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
    public Set<KeySelectorItem> filterItems(ItemChangedEvent itemChangedEvent) {
        Set<KeySelectorItem> newItems = new HashSet<>();
        Data item = itemChangedEvent.item;
        int numberOfPixel = itemChangedEvent.telescope.type.numberOfPixel;
        for  (String key: item.keySet()){
            try {
                double[][] data = (double[][]) item.get(key);
                if(data.length == itemChangedEvent.telescope.type.numberOfPixel) {
                    newItems.add(new KeySelectorItem(new LinePlotData(data, Color.GRAY, key), this));
                }
            } catch (ClassCastException e){
//                Do nothing and continue loop.
            }
        }

        double[][] data = new double[numberOfPixel][itemChangedEvent.roi];
        for (int pixel = 0; pixel < numberOfPixel; pixel++) {
            for (int slice = 0; slice < itemChangedEvent.roi; slice++) {
                data[pixel][slice] = itemChangedEvent.rawData[pixel][slice];
            }
        }

        newItems.add(new KeySelectorItem(new LinePlotData(data, Color.LIGHT_GRAY, "@event"), this));
        return newItems;
    }


}
