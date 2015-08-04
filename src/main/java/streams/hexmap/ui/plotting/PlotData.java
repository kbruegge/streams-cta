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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlotData plotData = (PlotData) o;

        if (color != null ? !color.equals(plotData.color) : plotData.color != null) return false;
        return !(name != null ? !name.equals(plotData.name) : plotData.name != null);
    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
