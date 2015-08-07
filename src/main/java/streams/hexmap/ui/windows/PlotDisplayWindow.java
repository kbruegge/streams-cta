package streams.hexmap.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import streams.hexmap.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import streams.hexmap.ui.components.SelectedLinePlotPanel;
import streams.hexmap.ui.components.selectors.IntervallMarkerKeySelector;
import streams.hexmap.ui.components.selectors.LinePlotSelector;
import streams.hexmap.ui.events.ItemChangedEvent;
import streams.hexmap.ui.events.PlotSelectionChangedEvent;

import javax.swing.*;

public class PlotDisplayWindow implements EventObserver{

	private final SelectedLinePlotPanel plotPanel = new SelectedLinePlotPanel(750, 480);
    private final LinePlotSelector keySelector = new LinePlotSelector();
    private final IntervallMarkerKeySelector intervalKeySelector = new IntervallMarkerKeySelector();
    private TelescopeEvent telescopeEvent;

    public PlotDisplayWindow() {
        Bus.eventBus.register(this);
	}

    public void showWindow(){
        JFrame frame = new JFrame();
        FormLayout layout = new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("pref"),
                ColumnSpec.decode("pref")
        },
                new RowSpec[]{
                        RowSpec.decode("pref"),
                        RowSpec.decode("240px"),
                        RowSpec.decode("pref"),
                        RowSpec.decode("240px")
                }
        );

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        builder.add(plotPanel, cc.xywh(1,1,1,4));
        builder.addSeparator("Series Selection", cc.xy(2, 1));
        builder.add(keySelector, cc.xy(2, 2));
        builder.addSeparator("Marker Selection", cc.xy(2, 3));
        builder.add(intervalKeySelector, cc.xy(2, 4));

        frame.setContentPane(builder.getPanel());
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }


//    @Subscribe
//    public void handleKeySelectionChange(IntervallMarkerSelectionChangedEvent e){
//        plotPanel.drawPlot(keySelector.getPlotData(), intervalKeySelector.getPlotData());
//    }


    @Subscribe
    public void handleKeySelectionChange(PlotSelectionChangedEvent e){
        plotPanel.drawPlot(keySelector.getSelectedPlotData(), intervalKeySelector.getSelectedPlotData());
    }

    @Override
    public void handleEventChange(ItemChangedEvent itemChangedEvent) {
        keySelector.updateSelectionItems(itemChangedEvent);
        plotPanel.drawPlot(keySelector.getSelectedPlotData(), intervalKeySelector.getSelectedPlotData());
    }


    public static void main(String[]args){
        PlotDisplayWindow p = new PlotDisplayWindow();
        p.showWindow();
    }

}
