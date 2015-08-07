package streams.hexmap.ui.components.selectors;

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
public class KeySelectorItem<T extends PlotData> extends JPanel  {
    private final JButton colorButton;
    private final JLabel label;
    private JCheckBox checkBox;

    public final T plotData;


    @Override
    public int hashCode(){
        return label.getText().hashCode();
    }

    public KeySelectorItem(final T plotData, final KeySelector selector){
        this.plotData = plotData;
        setLayout(new BorderLayout(0, 0));
        colorButton = new JButton("Color");
        colorButton.setForeground(this.plotData.getColor());
        colorButton.setPreferredSize(new Dimension(90, 25));
        setMaximumSize(new Dimension(280, 30));
        setPreferredSize(new Dimension(250, 30));

        label = new JLabel(this.plotData.getName());
        checkBox = new JCheckBox();
        checkBox.addItemListener(e -> {
            if(e.getStateChange()==ItemEvent.SELECTED){
                selector.addSelected(KeySelectorItem.this);
            } else if (e.getStateChange()==ItemEvent.DESELECTED){
                selector.removeSelected(KeySelectorItem.this);
            }
        });

        //If you click to select a new color this will be automatically activate the checkbox.
        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(null, "Choose color", Color.DARK_GRAY);
            KeySelectorItem.this.plotData.setColor(color);
            colorButton.setForeground(color);
            selector.addSelected(KeySelectorItem.this);
            checkBox.setSelected(true);
        });
        add(checkBox, BorderLayout.WEST);
        add(colorButton, BorderLayout.EAST);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeySelectorItem)) return false;
        KeySelectorItem<?> that = (KeySelectorItem<?>) o;
        return this.label.getText().equals(that.label.getText());
    }

    public void setSelected(boolean flag){
        this.checkBox.setSelected(flag);
    }
    public void setColor(Color color){
        this.colorButton.setForeground(color);
        this.plotData.setColor(color);
    }
    public Color getColor(){
        return this.plotData.getColor();
    }
}
