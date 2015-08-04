package streams.hexmap.ui.events;

import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.PlotData;

import java.util.Set;

/**
 * Created by kaibrugge on 16.05.14.
 */
public class PlotSelectionChangedEvent {
    public final Set<PlotData> selectedPlotData;

    public PlotSelectionChangedEvent(Set<PlotData> kl){
        this.selectedPlotData = kl;
    }
}
