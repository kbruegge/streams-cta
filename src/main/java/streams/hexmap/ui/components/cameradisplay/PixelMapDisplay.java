package streams.hexmap.ui.components.cameradisplay;

import streams.hexmap.ui.colormaps.ColorMapping;

public interface PixelMapDisplay {

    void setColorMap(ColorMapping m);

    int getWidth();

    int getHeight();

    Tile[] getTiles();

}
