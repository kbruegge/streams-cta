package streams.hexmap.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.components.MainPlotPanel;
import streams.hexmap.ui.components.selectors.IntervallMarkerKeySelector;
import streams.hexmap.ui.components.selectors.TelescopeEventSelector;
import streams.hexmap.ui.events.IntervallMarkerSelectionChangedEvent;
import streams.hexmap.ui.events.PlotSelectionChangedEvent;

import javax.swing.*;

public class PlotDisplayWindow {

	private final MainPlotPanel plotPanel = new MainPlotPanel(750, 480, false);
    private final TelescopeEventSelector keySelector = new TelescopeEventSelector();
    private final IntervallMarkerKeySelector intervalKeySelector = new IntervallMarkerKeySelector();

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


    @Subscribe
    public void handleKeySelectionChange(IntervallMarkerSelectionChangedEvent e){
        plotPanel.drawPlot(keySelector.getPlotData(), intervalKeySelector.getPlotData());
    }

    @Subscribe
    public void handleKeySelectionChange(PlotSelectionChangedEvent e){
        plotPanel.drawPlot(keySelector.getPlotData(), intervalKeySelector.getPlotData());
    }

    public static void main(String[]args){
        PlotDisplayWindow p = new PlotDisplayWindow();
        p.showWindow();
    }

}
