package streams.hexmap.ui.plotting;

import java.awt.*;

/**
 * Contains all information needed to plot some data into the jfreechart linechart.
 * Created by kai on 21.07.15.
 */
public class LinePlotData extends PlotData {

    double[][] plotData;

    public LinePlotData(double[][] plotData, Color color, String name) {
        super(color,name);

        this.plotData = plotData;
    }

    public double[][] getPlotData() {
        return plotData;
    }
}
