/**
 * 
 */
package streams.hexmap.ui.components;

import com.google.common.eventbus.Subscribe;
import streams.hexmap.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.PixelSelectionObserver;
import streams.hexmap.ui.SliceObserver;
import streams.hexmap.ui.events.SliceChangedEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import streams.hexmap.ui.plotting.IntervalPlotData;
import streams.hexmap.ui.plotting.LinePlotData;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * This is the panel containing the jfreechart plotting stuff. It can plot the timeseries for selected pixels or
 * if showAverage is True show the averaged timeseries over all pixel
 *
 *  @author Kai
 */
public abstract class PlotPanel extends JPanel implements SliceObserver{

	/** The unique class ID */
	private static final long serialVersionUID = -4365922853855318209L;

    final XYPlot plot;
	private ValueMarker sliceMarker;

    public boolean hasTicks = false;

    private int currentSlice = 0;

    public void setRange(int min, int max){
        plot.getDomainAxis().setRange(min, max);
    }

    public PlotPanel(int width, int height) {

        Bus.eventBus.register(this);

        plot = new XYPlot(null, new NumberAxis("Slice"), new NumberAxis(), null);

        //enable panning
        plot.setDomainPannable(true);
        plot.setRangePannable(true);


        //draw the dashed line showing the current slice
        addSliceMarkerToPlot();


        final JFreeChart chart = new JFreeChart(plot);
        final ChartPanel p = new ChartPanel(chart);
        p.setPreferredSize(new Dimension(width, height));
        //setPreferredSize(new Dimension(width, height));


        //Add an item to the context menu to toggle display of Ticks in the plot
        JCheckBoxMenuItem mI = new JCheckBoxMenuItem("Show Ticks", false);
        mI.addActionListener(e -> {
            hasTicks = !hasTicks;
            int count = plot.getRendererCount();
            for (int i = 0; i < count ; i++){
                XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer(i);
                r.setBaseShapesVisible(hasTicks);
            }
        });
        p.getPopupMenu().add(mI);

        //add plot to the current component
        add(p, BorderLayout.CENTER);
    }


//    private void prepareAndDrawPlot(Data item, TelescopeEvent telescopeEvent) {
//        clearPlot();
//        addSliceMarkerToPlot();
//        drawPlot(item, telescopeEvent);
//    }

    public abstract void drawPlot(Set<LinePlotData> linePlots, Set<IntervalPlotData> intervalPlots);
//    protected abstract void drawPlot(Data item, TelescopeEvent telescopeEvent);


    @Override
    @Subscribe
    public void handleSliceChangeEvent(SliceChangedEvent ev) {
        this.currentSlice = ev.currentSlice;
        sliceMarker.setValue(ev.currentSlice);
    }


//    @Override
//    @Subscribe
//    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel) {
//        this.selectedPixel.clear();
//        this.selectedPixel.addAll(selectedPixel);
//    }



    protected void clearPlot(){
        for (int i = 0; i < plot.getDatasetCount(); i++){
            XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset(i);
            if (dataset != null) {
                dataset.removeAllSeries();
            }
        }
        //get rid of all domain markers
        plot.clearDomainMarkers();
    }

    /**
     * adds the slice marker to the plot
     */
    protected void addSliceMarkerToPlot() {
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


	protected XYSeries createSeries(String name, double[] data) {
		XYSeries series = new XYSeries(name);
		for (int i = 0; i < data.length; i++) {
			series.add((double) i, data[i]);
		}
		return series;
	}
}
