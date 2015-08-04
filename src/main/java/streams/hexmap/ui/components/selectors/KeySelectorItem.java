package streams.hexmap.ui.components.selectors;

import streams.hexmap.ui.plotting.LinePlotData;
import streams.hexmap.ui.plotting.PlotData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This is a little gui element containing a checkbox and a button to choose the color.
 * Created by kaibrugge on 14.05.14.
 */
public class KeySelectorItem extends JPanel  {
    private final JButton colorButton;
    private final JLabel label;
    private JCheckBox checkBox;
    public final PlotData linePlotData;

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        return linePlotData.equals(obj);
    }
    @Override
    public int hashCode(){
        return linePlotData.hashCode();
    }

    public KeySelectorItem(final PlotData plotData, final KeySelector selector){
        this.linePlotData = plotData;
        setLayout(new BorderLayout(0, 0));
        colorButton = new JButton("Color");
        colorButton.setForeground(linePlotData.getColor());
        colorButton.setPreferredSize(new Dimension(90, 25));
        setMaximumSize(new Dimension(280, 30));
        setPreferredSize(new Dimension(250, 30));

        label = new JLabel(linePlotData.getName());
        checkBox = new JCheckBox();
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    selector.addSelected(KeySelectorItem.this);
                } else if (e.getStateChange()==ItemEvent.DESELECTED){
                    selector.removeSelected(KeySelectorItem.this);
                }
            }
        });

        //If you click to select a new color this will be automatically activate the checkbox.
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(null, "Choose color", Color.DARK_GRAY);
                linePlotData.setColor(color);
                colorButton.setForeground(color);
                selector.addSelected(KeySelectorItem.this);
                checkBox.setSelected(true);
            }
        });
        add(checkBox, BorderLayout.WEST);
        add(colorButton, BorderLayout.EAST);
        add(label, BorderLayout.CENTER);
    }
}
