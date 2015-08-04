package streams.hexmap.ui.components.selectors;

import streams.hexmap.ui.Bus;
import streams.hexmap.ui.events.OverlaySelectionChangedEvent;
import streams.hexmap.ui.overlays.CameraMapOverlay;
import stream.Data;
import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.OverlayPlotData;
import streams.hexmap.ui.plotting.PlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The overlay selector will be shown next to a camerawindow which displays the overlays.
 * Created by kaibrugge on 02.06.14.
 */
public class CameraOverlayKeySelector extends KeySelector {

    @Override
    public void selectionUpdate() {
        Bus.eventBus.post(new OverlaySelectionChangedEvent(this.getPlotData()));
    }

    @Override
    public Set<KeySelectorItem> filterItems(Data item) {
        Set<KeySelectorItem> newItems = new HashSet<>();
        for  (String key: item.keySet()){
            try {
                CameraMapOverlay b = (CameraMapOverlay) item.get(key);
                newItems.add(new KeySelectorItem(new OverlayPlotData(b, Color.GRAY, key), this));
            } catch (ClassCastException e){
                continue;
            }
        }
        return newItems;
    }

    @Override
    public Set<OverlayPlotData> getPlotData() {
        return null;
    }


}
