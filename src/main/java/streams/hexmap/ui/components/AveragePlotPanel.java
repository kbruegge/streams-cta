package streams.hexmap.ui.components;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import streams.hexmap.ui.plotting.IntervalPlotData;
import streams.hexmap.ui.plotting.LinePlotData;


import java.awt.*;
import java.util.Set;


/**
 * Created by kai on 04.08.15.
 */
public class AveragePlotPanel extends PlotPanel {

    public AveragePlotPanel(int width, int height) {
        super(width, height);
    }

    @Override
    public void drawPlot(Set<LinePlotData> linePlots, Set<IntervalPlotData> intervalPlots) {
        //        clearPlot();
        //        addSliceMarkerToPlot();
        for (LinePlotData linePLot : linePlots){

            double [][] data = linePLot.getPlotData();
            drawPlot(data);
        }

    }

    public void drawPlot(double[][] data) {
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        r.setSeriesPaint(0, Color.RED);
        r.setBaseShapesVisible(hasTicks);
        //we also create a new dataset for each key
        XYSeriesCollection dataset =  new XYSeriesCollection();

        double[] average = averageSlicesForEachPixel(data);
        dataset.addSeries(createSeries("Mean voltage in all pixels", average));

        plot.setDataset(0, dataset);
        plot.setRenderer(0, r);

        //call this to update the plot
        plot.datasetChanged(null);
    }


    /**
     *
     * @param data the short[][] containing the data
     * @return an array of length roi containing the slice averages
     */
    private double[] averageSlicesForEachPixel(double[][] data) {
        double[] average = new double[data[0].length];
        int slice = 0;
        for (double[] slices : data) {
            for (double s : slices) {
                average[slice] += s;
                slice++;
            }
            slice = 0;
        }
        for (int i = 0; i < average.length; i++) {
            average[i] /= (double) data.length;
        }
        return average;
    }
}
