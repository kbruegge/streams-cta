package streams.hexmap.ui.plotting;

import streams.hexmap.ui.overlays.CameraMapOverlay;

import java.awt.*;

/**
 * Contains all information needed to plot an intervalMarkers into the camera picture
 * Created by kai on 21.07.15.
 */
public class OverlayPlotData extends PlotData {

    final CameraMapOverlay overlay;

    public OverlayPlotData(CameraMapOverlay overlay, Color color, String name) {
        super(color,name);
        this.overlay = overlay;
    }

    public CameraMapOverlay getOverlay() {
        return overlay;
    }
}
