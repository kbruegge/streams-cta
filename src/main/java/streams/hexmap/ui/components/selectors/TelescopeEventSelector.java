package streams.hexmap.ui.components.selectors;

import streams.Utils;
import streams.cta.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.PlotSelectionChangedEvent;
import stream.Data;
import streams.hexmap.ui.overlays.CameraMapOverlay;
import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.OverlayPlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 21.07.15.
 */
public class TelescopeEventSelector extends KeySelector {


    @Override
    public void selectionUpdate() {
        Bus.eventBus.post(new PlotSelectionChangedEvent(null));
    }

    @Override
    public Set<KeySelectorItem> filterItems(Data item) {
        Set<KeySelectorItem> newItems = new HashSet<>();
        for  (String key: item.keySet()){
            try {
                TelescopeEvent b = (TelescopeEvent) item.get(key);
                double[][] data = new double[b.numberOfPixel][b.roi];
                for (int pixel = 0; pixel < data.length; pixel++) {
                    for (int slice = 0; slice < b.roi; slice++) {
                        data[pixel][slice] = b.data[pixel][slice];
                    }
                }
                newItems.add(new KeySelectorItem(new LinePlotData(data, Color.DARK_GRAY, key), this));
            } catch (ClassCastException e){
                continue;
            }
        }
        return newItems;
    }

    @Override
    public Set<LinePlotData> getPlotData() {
        return ;
    }
}
