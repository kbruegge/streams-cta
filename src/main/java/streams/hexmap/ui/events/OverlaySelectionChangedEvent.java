package streams.hexmap.ui.events;

import org.apache.commons.math3.util.Pair;
import streams.hexmap.ui.plotting.OverlayPlotData;

import java.awt.*;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class OverlaySelectionChangedEvent {
    public final Set<OverlayPlotData> selectedItems;
    public OverlaySelectionChangedEvent(Set<OverlayPlotData> kl){
        this.selectedItems = kl;
    }

}
