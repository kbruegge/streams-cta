package streams.hexmap.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import streams.Utils;
import streams.hexmap.ui.Bus;
import streams.hexmap.ui.EventObserver;
import streams.hexmap.ui.components.cameradisplay.CameraDisplayPanel;
import stream.Data;
import streams.hexmap.ui.events.ItemChangedEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This should be able to plot all data in form of a double array which has a length of N*1440 where N is a natural
 * number >= 1.
 *
 * Created by kaibrugge on 13.05.14.
 */
public class CameraWindow implements EventObserver {
    private final CameraDisplayPanel hexMapDisplay;
    private final JComboBox<String> keyComboBox = new JComboBox<>();
    private Data dataItem;


    /**
     * The window takes a key to some entry in the Data item which it will display
     * @param key
     */
    public CameraWindow(String key){
        keyComboBox.addItem(key);
        keyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) ((JComboBox)e.getSource()).getSelectedItem();
                if(key != null) {
                    hexMapDisplay.setItemToDisplay(key, dataItem);
                }
            }
        });


        hexMapDisplay = new CameraDisplayPanel();
        hexMapDisplay.setBackground(Color.BLACK);

        Bus.eventBus.register(this);
    }

    /**
     * Define the layout for the window
     */
    public void showWindow(){
        JFrame frame = new JFrame();

        // set layout of the main window
        frame.getContentPane().setLayout(
                new FormLayout( new ColumnSpec[]{
                                    ColumnSpec.decode("right:max(500;pref):grow"),},
                                new RowSpec[]{
                                    RowSpec.decode("pref"),
                                    RowSpec.decode("default"),
                                }
                )
        );
        frame.getContentPane().add(keyComboBox, "1,1,left,top");
        frame.getContentPane().add(hexMapDisplay, "1,2,left,top");
        frame.getContentPane().setBackground(Color.BLACK);

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    /**
     * Adds the keys we can display in the camera window to the dropdown list.
     * @param itemChangedEvent the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(ItemChangedEvent itemChangedEvent) {
        this.dataItem = itemChangedEvent.item;
        String selectedKey = (String) keyComboBox.getSelectedItem();
        keyComboBox.removeAllItems();
        for(String key : dataItem.keySet()){
            double[] data = Utils.toDoubleArray(dataItem.get(key));
            if(data != null && data.length > 0 && data.length%itemChangedEvent.telescopeEvent.roi == 0) {
                keyComboBox.addItem(key);
                if(key.equals(selectedKey)){
                    keyComboBox.setSelectedItem(key);
                    selectedKey = key;
                }
            }
        }
    }
}
