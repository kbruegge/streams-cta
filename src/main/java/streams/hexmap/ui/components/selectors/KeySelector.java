package streams.hexmap.ui.components.selectors;

import com.google.common.eventbus.Subscribe;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import stream.Data;
import streams.hexmap.ui.events.ItemChangedEvent;
import streams.hexmap.ui.plotting.PlotData;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A KeySelector is a JPanel which lays out KeySelectorItems. It keeps track of which KeySelectorItems are selected
 * in the selectedItems HashSet. In case a new Event (DataItem) will be shown in the viewer, this class will call
 * the abstract filterItems(Data item) method.
 *
 * The filterItems(Data item) should return a Set<KeySelectorItem> which will be shown in the JPanel
 *
 *
 *
 * Created by kaibrugge on 15.05.14.
 */
public abstract class  KeySelector extends JPanel implements EventObserver{

    private final JPanel keySelectionContentPanel = new JPanel();
    private final JScrollPane keyScrollPane = new JScrollPane(keySelectionContentPanel);

    private Set<KeySelectorItem> items = new HashSet<>();
    protected Set<KeySelectorItem> selectedItems = new HashSet<>();


    public KeySelector(){

        Bus.eventBus.register(this);

        setLayout(new BorderLayout());

        keySelectionContentPanel.setLayout(new BoxLayout(keySelectionContentPanel, BoxLayout.Y_AXIS));


        keyScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(keyScrollPane, BorderLayout.WEST);
    }
    @Override
    public void setPreferredSize(Dimension preferredSize){
        super.setPreferredSize(preferredSize);
        this.keyScrollPane.setPreferredSize(preferredSize);
    }

    @Override
    @Subscribe
    public void handleEventChange(ItemChangedEvent itemChangedEvent){

        Set<KeySelectorItem> newItems = filterItems(itemChangedEvent.item);
        //keep old items selected
        selectedItems.retainAll(newItems);
        //keep old items on the display and add new ones. This is a cut and a union
        items.retainAll(newItems);
        items.addAll(newItems);

        keySelectionContentPanel.removeAll();
        for(KeySelectorItem k : items){
            k.setAlignmentX(Component.LEFT_ALIGNMENT);
            keySelectionContentPanel.add(k);
        }
        keySelectionContentPanel.revalidate();
        keySelectionContentPanel.repaint();
    }

    public Set<PlotData> getSelectedPlotData(){
        Set<PlotData> selectedPlotData = new HashSet<>();
        for(KeySelectorItem k : selectedItems) {
            selectedPlotData.add(k.linePlotData);
        }
        return selectedPlotData;
    }


    public void addSelected(KeySelectorItem selectedItem) {
        selectedItems.add(selectedItem);
        selectionUpdate();
    }

    public void removeSelected(KeySelectorItem deselectedItem) {
        selectedItems.remove(deselectedItem);
        selectionUpdate();
    }

    public abstract void selectionUpdate();
    public abstract Set<KeySelectorItem> filterItems(Data item);
    public abstract Set<? extends PlotData> getPlotData();

}


