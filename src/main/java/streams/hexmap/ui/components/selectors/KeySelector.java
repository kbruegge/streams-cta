package streams.hexmap.ui.components.selectors;

import com.google.common.eventbus.Subscribe;
import streams.hexmap.TelescopeEvent;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import stream.Data;
import streams.hexmap.ui.events.ItemChangedEvent;
import streams.hexmap.ui.events.PlotSelectionChangedEvent;
import streams.hexmap.ui.plotting.PlotData;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A KeySelector is a JPanel which lays out KeySelectorItems. It keeps track of which KeySelectorItems are selected
 * in the selectedItems HashSet. In case a new Event (DataItem) will be shown in the viewer, this class will call
 * the abstract filterItems(Data item) method.
 *
 * The filterItems(Data item) should return a Set<KeySelectorItem> which will be shown in the JPanel
 *
 * Created by kaibrugge on 15.05.14.
 */
public abstract class  KeySelector<T extends PlotData> extends JPanel{

    private final JPanel keySelectionContentPanel = new JPanel();
    private final JScrollPane keyScrollPane = new JScrollPane(keySelectionContentPanel);


    private Set<KeySelectorItem<T>> items = new HashSet<>();
    protected Map<String, KeySelectorItem<T>> selectedItems = new HashMap<>();


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


    public void updateSelectionItems(ItemChangedEvent itemChangedEvent){
        //TODO remove old selection items and replace with new ones.
        Set<KeySelectorItem<T>> newItems = filterItems(itemChangedEvent);

        for(KeySelectorItem item : newItems){
            KeySelectorItem oldItem = selectedItems.get(item.plotData.getName());
            if(oldItem != null){
                    item.setSelected(true);
                    item.setColor(oldItem.getColor());
                    selectedItems.replace(item.plotData.getName(), item);
                }
        }

        items.clear();
        items.addAll(newItems);


        keySelectionContentPanel.removeAll();
        for(KeySelectorItem k : items){
            k.setAlignmentX(Component.LEFT_ALIGNMENT);
            keySelectionContentPanel.add(k);
        }
        keySelectionContentPanel.revalidate();
        keySelectionContentPanel.repaint();
    }

    public Set<T> getSelectedPlotData(){
        Set<T> selectedPlotData = new HashSet<>();
        selectedItems.forEach((key, value) -> selectedPlotData.add(value.plotData));
        return selectedPlotData;
    }

    /**
     * This method will be called by a selectoritem in case the checkbox is checked
     * @param selectedItem the item that has been selected
     */
    public void addSelected(KeySelectorItem selectedItem) {
        selectedItems.put(selectedItem.plotData.getName(), selectedItem);
        Bus.eventBus.post(new PlotSelectionChangedEvent());
    }

    /**
     * This method will be called by a selectoritem in case the checkbox is unchecked
     * @param deselectedItem the item that has been deselected
     */
    public void removeSelected(KeySelectorItem deselectedItem) {
        selectedItems.remove(deselectedItem);
        Bus.eventBus.post(new PlotSelectionChangedEvent());
    }

//    public abstract void selectionUpdate();
    public abstract Set<KeySelectorItem<T>> filterItems(ItemChangedEvent itemChangedEvent);

}


