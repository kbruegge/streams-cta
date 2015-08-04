package streams.hexmap.ui.plotting;

import org.jfree.chart.plot.IntervalMarker;

import java.awt.*;

/**
 * Contains all information needed to plot an interval int the jfreechart lineplot
 * Created by kai on 21.07.15.
 */
public class IntervalPlotData extends PlotData {

    final IntervalMarker[] intervalMarkers;

    public IntervalPlotData(IntervalMarker[] overlay, Color color, String name) {
        super(color,name);
        this.intervalMarkers = overlay;
    }

    public IntervalMarker[] getIntervalMarkers() {
        return intervalMarkers;
    }
}
