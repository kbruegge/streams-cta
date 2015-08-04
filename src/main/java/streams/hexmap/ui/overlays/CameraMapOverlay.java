package streams.hexmap.ui.overlays;

import streams.hexmap.ui.components.cameradisplay.HexMapDisplay;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by kaibrugge on 28.05.14.
 */
public interface CameraMapOverlay extends Serializable {
    public void setColor(Color c);
    public void paint(Graphics2D g2, HexMapDisplay map);
    public int getDrawRank();	// Wie hoeher die Nummer je spaeter wird es gezeichnet -> hoehere Prioritaet
}
