/**
 * 
 */
package streams.hexmap.ui.components;

import com.google.common.eventbus.Subscribe;
import streams.Utils;
import streams.cta.TelescopeEvent;
import streams.hexmap.CameraPixel;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import streams.hexmap.ui.PixelSelectionObserver;
import streams.hexmap.ui.SliceObserver;
import streams.hexmap.ui.events.ItemChangedEvent;
import streams.hexmap.ui.events.SliceChangedEvent;
import org.apache.commons.math3.util.Pair;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import stream.Data;
import streams.hexmap.ui.plotting.IntervalPlotData;
import streams.hexmap.ui.plotting.LinePlotData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the panel containing the jfreechart plotting stuff. It can plot the timeseries for selected pixels or
 * if showAverage is True show the averaged timeseries over all pixel
 *
 *  @author Kai
 */
public class MainPlotPanel extends JPanel implements SliceObserver, PixelSelectionObserver{

	/** The unique class ID */
	private static final long serialVersionUID = -4365922853855318209L;

    final XYPlot plot;
	private ValueMarker sliceMarker;

    private boolean showAverage = false;
    private boolean hasTicks = false;

    private Set<CameraPixel> selectedPixel = new HashSet<>();

    private int currentSlice = 0;

    public void setRange(int min, int max){
        plot.getDomainAxis().setRange(min , max);
    }

    public MainPlotPanel(int width, int height, boolean showAverage) {

        Bus.eventBus.register(this);

        plot = new XYPlot(null, new NumberAxis("Slice"), new NumberAxis(),
                null);
        this.showAverage = showAverage;

        /**
         * enable panning
         */
        plot.setDomainPannable(true);
        plot.setRangePannable(true);


        //draw the dashed line showing the current slice
        addSliceMarkerToPlot();


        final JFreeChart chart = new JFreeChart(plot);
        final ChartPanel p = new ChartPanel(chart);
        p.setPreferredSize(new Dimension(width, height));
        //setPreferredSize(new Dimension(width, height));

        /**
         * Add an item to the context menu to toggle display pf Ticks in the plot.
         */
        JCheckBoxMenuItem mI = new JCheckBoxMenuItem("Show Ticks", false);
        mI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hasTicks = !hasTicks;
                int count = plot.getRendererCount();
                for (int i = 0; i < count ; i++){
                    XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer(i);
                    r.setBaseShapesVisible(hasTicks);
                }
            }
        });
        p.getPopupMenu().add(mI);

        //add plot to the current component
        add(p, BorderLayout.CENTER);
    }



    @Override
    @Subscribe
    public void handleSliceChangeEvent(SliceChangedEvent ev) {
        this.currentSlice = ev.currentSlice;
        sliceMarker.setValue(ev.currentSlice);
    }


    @Override
    @Subscribe
    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel) {
        this.selectedPixel.clear();
        this.selectedPixel.addAll(selectedPixel);
    }



    public void clearPlot(){
        for (int i = 0; i < plot.getDatasetCount(); i++){
            XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset(i);
            if (dataset != null) {
                dataset.removeAllSeries();
            }
        }
        //get rid of all domain markers
        plot.clearDomainMarkers();
    }

    public void drawPlot(Set<LinePlotData> dataToPLot, Set<IntervalPlotData> intervalsToPLot) {
        clearPlot();
        addSliceMarkerToPlot();

        //we will be adding multiple datasets to the plot. each dataset needs an index.
        int dataSetCounter = 0;

        //iterate over all the selected keys with their corresponding colors
        for (LinePlotData linePlotData: dataToPLot){
            double[][] data = linePlotData.getPlotData();
            Color c = linePlotData.getColor();
            String name = linePlotData.getName();
            if (data != null ){
                //for each key we have to plot we create a new renderer so we can have a custom color for every key
                XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
                r.setSeriesPaint(0, c);
                r.setBaseShapesVisible(hasTicks);
                //we also create a new dataset for each key
                XYSeriesCollection dataset =  new XYSeriesCollection();
                if(showAverage) {
//                    double[] average = Utils.averageSlicesForEachPixel(data, roi);
//                    dataset.addSeries(createSeries(name, average));
                }else{
                    //for each pixel add a new series to the dataset
                    int seriesCounter = 0;
                    for(CameraPixel p : selectedPixel) {
                        r.setSeriesPaint(seriesCounter, c);
                        dataset.addSeries(createSeriesForPixel(name+"_" + p.id, data[p.id]));
                        seriesCounter++;
                    }
                }
                plot.setDataset(dataSetCounter, dataset);
                plot.setRenderer(dataSetCounter, r);
            }

            dataSetCounter++;
        }

        for (IntervalPlotData m : intervalsToPLot) {
                for(CameraPixel p: selectedPixel){
                    IntervalMarker marker = m.getIntervalMarkers()[p.id];
                    marker.setPaint(m.getColor());
                    plot.addDomainMarker(marker);
                }
        }
        //call this to update the plot
        plot.datasetChanged(null);
    }

    /**
     * adds the slice marker to the plot
     */
    private void addSliceMarkerToPlot() {
        sliceMarker = new ValueMarker(currentSlice);
        sliceMarker.setPaint(Color.gray);
        sliceMarker.setStroke(new BasicStroke(1.0f, // Width
            BasicStroke.CAP_SQUARE, // End cap
            BasicStroke.JOIN_MITER, // Join style
            10.0f, // Miter limit
            new float[]{10.0f, 10.0f}, // Dash pattern
            0.0f)); // Dash phase
        sliceMarker.setLabel("Slice");
        plot.addDomainMarker(sliceMarker);
    }


    private XYSeries createSeriesForPixel(String name, double[] pixelData) {
        XYSeries series = new XYSeries(name);
		for (int i = 0; i < pixelData.length; i++) {
			series.add(i, pixelData[i]);
		}
		return series;
	}

	public XYSeries createSeries(String name, double[] data) {
		XYSeries series = new XYSeries(name);
		for (int i = 0; i < data.length; i++) {
			series.add((double) i, data[i]);
		}
		return series;
	}
}
