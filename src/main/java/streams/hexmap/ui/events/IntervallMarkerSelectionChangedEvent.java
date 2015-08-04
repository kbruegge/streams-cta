package streams.hexmap.ui.events;

import org.apache.commons.math3.util.Pair;
import streams.hexmap.ui.plotting.IntervalPlotData;

import java.awt.*;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class IntervallMarkerSelectionChangedEvent {
    public final Set<IntervalPlotData> selectedItems;
    public IntervallMarkerSelectionChangedEvent(Set<IntervalPlotData> kl){
        this.selectedItems = kl;
    }

}
