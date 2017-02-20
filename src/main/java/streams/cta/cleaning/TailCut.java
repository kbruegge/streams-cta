package streams.cta.cleaning;

import stream.Data;
import stream.annotations.Parameter;
import streams.cta.CTARawDataProcessor;
import streams.hexmap.Shower;

import java.util.Map;

/**
 * A heuristic to find signal pixels in the image.
 * Its based on a ideas from the equivalent fact-tools processors, HESS methods and some things in ctapipe.
 *
 *  @author  Kai Bruegge on 14.02.17
 */
public class TailCut extends CTARawDataProcessor {

    @Parameter(required = false)
    public Double[] levels = {10.0, 8.0, 4.5};


    @Override
    public Data process(Data input, Map<Integer, double[]> images) {

        images.forEach((cameraId, image) -> {

            Shower shower = new Shower(cameraId);

            //add the pixels over the first threshold
            for(int pixelId = 0; pixelId < image.length; pixelId++)
            {
                double weight = image[pixelId];
                if (weight > levels[0]){
                    shower.addPixel(pixelId, weight);
                }
            }

            // dilate the shower
            for (int l = 1; l < levels.length; l++) {
                shower.dilate(image, levels[l]);
            }

            input.put("telescope:" + cameraId + ":shower", shower);
            input.put("telescope:" + cameraId + ":shower:number_of_pixel", shower.pixels.size());
        });

        return input;
    }
}
