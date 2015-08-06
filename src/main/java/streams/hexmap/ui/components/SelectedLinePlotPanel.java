package streams.hexmap.ui.components;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import streams.cta.TelescopeEvent;
import streams.hexmap.CameraPixel;
import streams.hexmap.ui.PixelSelectionObserver;
import streams.hexmap.ui.plotting.IntervalPlotData;
import streams.hexmap.ui.plotting.LinePlotData;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kai on 04.08.15.
 */
public class SelectedLinePlotPanel extends PlotPanel implements PixelSelectionObserver {

    private Set<CameraPixel> selectedPixel = new HashSet<>();
    Set<LinePlotData> linePlots;
    Set<IntervalPlotData> intervalPlots;

    public SelectedLinePlotPanel(int width, int height) {
        super(width, height);
    }


    @Override
    public void drawPlot(Set<LinePlotData> linePlots, Set<IntervalPlotData> intervalPlots, TelescopeEvent currentTelescopeEvent) {
        this.linePlots = linePlots;
        this.intervalPlots =  intervalPlots;
        clearPlot();
        addSliceMarkerToPlot();

        //we will be adding multiple datasets to the plot. each dataset needs an index.
        int dataSetCounter = 0;

        //iterate over all the selected keys with their corresponding colors
        for(LinePlotData linePlot : linePlots){
            //for each key we have to plot we create a new renderer so we can have a custom color for every key
            XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
            r.setSeriesPaint(0, linePlot.getColor());
            r.setBaseShapesVisible(hasTicks);
            //we also create a new dataset for each key
            XYSeriesCollection dataset =  new XYSeriesCollection();
            //for each pixel add a new series to the dataset
            int seriesCounter = 0;
            for(CameraPixel p : selectedPixel) {
                r.setSeriesPaint(seriesCounter, linePlot.getColor());
                dataset.addSeries(createSeriesForPixel(linePlot.getName()+" for pixel: " + p.id, linePlot.getPlotData()[p.id]));
                seriesCounter++;
            }
            plot.setDataset(dataSetCounter, dataset);
            plot.setRenderer(dataSetCounter, r);

            dataSetCounter++;
        }



        for (IntervalPlotData intervalPlotData : intervalPlots) {
            for(CameraPixel p: selectedPixel){
                plot.addDomainMarker(intervalPlotData.getIntervalMarkers()[p.id]);
            }
        }
        //call this to update the plot
        plot.datasetChanged(null);
    }

    private XYSeries createSeriesForPixel(String name, double[] pixelData) {
        XYSeries series = new XYSeries(name);
		for (int i = 0; i < pixelData.length; i++) {
			series.add(i, pixelData[i]);
		}
		return series;
	}

    @Override
    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel) {
        this.selectedPixel = selectedPixel;
        drawPlot(linePlots, intervalPlots, null);
    }
}
