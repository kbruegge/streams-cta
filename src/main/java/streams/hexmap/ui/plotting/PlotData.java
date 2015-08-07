package streams.hexmap.ui.plotting;

import java.awt.*;

/**
 * Created by kai on 21.07.15.
 */
public class PlotData {

    private Color color;
    private String name;

    public PlotData(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
