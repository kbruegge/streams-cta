package streams.hexmap.ui.components.cameradisplay;

import streams.hexmap.ui.colormaps.ColorMapping;

public interface PixelMapDisplay {

    void setColorMap(ColorMapping m);

    /**
     * A PixelMap has a number of tiles it can display. In case of the PixelMap for the fact camera this would be 1440 pixel
     * @return the number of tiles displayed
     */
    int getNumberOfTiles();

    int getWidth();

    int getHeight();

    Tile[] getTiles();

}
