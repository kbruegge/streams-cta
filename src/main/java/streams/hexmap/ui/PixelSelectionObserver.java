package streams.hexmap.ui;

import com.google.common.eventbus.Subscribe;
import streams.hexmap.CameraPixel;

import java.util.Set;

/**
 * Created by kaibrugge on 29.04.14.
 */
public interface PixelSelectionObserver {


    /**
     * This observer should react to changes in the pixel selection. .
     *
     * @param selectedPixel  the set containing the currently selected CameraPixel
     */
    @Subscribe
    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel);

}
