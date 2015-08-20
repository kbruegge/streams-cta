package streams.cta.cleaning;

import stream.Data;
import stream.annotations.Parameter;
import streams.cta.CTAExtractedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.CTAHexPixelMapping;
import streams.hexmap.CameraPixel;
import streams.hexmap.ui.overlays.PixelSetOverlay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by kai on 20.08.15.
 */
public class TwoLevelCleaning extends CTAExtractedDataProcessor {

    @Parameter(required = false)
    double[] levels = {3500,3200};

    HashSet<CameraPixel> showerPixel = new HashSet<>();

    CTAHexPixelMapping pixelMap = CTAHexPixelMapping.getInstance();

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons, double[] arrivalTimes) {

        for (int i = 0; i < photons.length; i++) {
            if (photons[i] > levels[0]){
                showerPixel.add(pixelMap.getPixelFromId(i));
            }
        }

        if(showerPixel.size() < 5 ){
            return input;
        }

        input.put("shower", showerPixel);
        input.put("@showerOverlay",new PixelSetOverlay(showerPixel));

        return input;
    }

    public void setLevels(double[] levels) {
        this.levels = levels;
    }
}
