package streams.hexmap.ui.overlays;

import streams.hexmap.CameraPixel;
import streams.hexmap.FactHexPixelMapping;
import streams.hexmap.HexPixelMapping;
import streams.hexmap.ui.components.cameradisplay.HexMapDisplay;
import streams.hexmap.ui.components.cameradisplay.Tile;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This is overlay can draw borders around the pixels passed to it via constructor or the add methods.
 */
public class PixelSetOverlay implements CameraMapOverlay, Serializable {
    Set<CameraPixel> set = new HashSet<>();
    Color c = Color.WHITE;

    public PixelSetOverlay(Set<CameraPixel> set){
        this.set = set;
    }

    public PixelSetOverlay(){
    }
    public void add(CameraPixel p){
        set.add(p);
    }

    @Override
    public void setColor(Color c) {
        this.c = c;
    }

    @Override
    public void paint(Graphics2D g2, HexMapDisplay map) {
        for (Tile t : map.getTiles()){
            if(set.contains(t.getCameraPixel())){
                if (t.getBorderColor() != Color.BLACK){
                    t.setBorderColor(Color.YELLOW);
                } else {
                    t.setBorderColor(this.c);
                }
                t.paint(g2);
            }
        }
    }

    public void clear() {
        set.clear();
    }

	@Override
	public int getDrawRank() {		
		return 1;
	}
}
