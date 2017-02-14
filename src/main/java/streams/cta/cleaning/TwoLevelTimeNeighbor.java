package streams.cta.cleaning;

import stream.Data;
import stream.annotations.Parameter;

import streams.cta.CTARawDataProcessor;

import streams.hexmap.Shower;

import java.util.Map;

/**
 * TwoLevelTimeNeighbor. Identifies showerPixel in the image array.
 *	 Cleaning in several Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  4) Calculate for each Pixel the difference in arrival times to the neighboring Pixels. Remove all pixel
 *     with less than 3 neighboring pixel with a difference smaller than timeLimit
 *  5) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 *  6) Remove Star Cluster (Cluster which contains only pixel around a known star position
 *
 *  based on fact-tools processor TwoLevelTimeNeighbor
 *
 *  @author  jens buss on 26.08.15
 */
public class TwoLevelTimeNeighbor extends CTARawDataProcessor {

    @Parameter(required = false)
    public Double[] levels = {6., 5., 4., 3.};


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

//            showerPixel = removeSmallCluster(showerPixel, 2);
            input.put(String.format("shower:%d", cameraId), shower);
        });

        return input;
    }


//
//    /**
//     * Remove all clusters of pixels with less than minNumberOfPixel pixels in the cluster
//     * @param showerPixel 'HashSet containing the so far identified shower pixels'
//     * @param minNumberOfPixel
//     * @return
//     */
//    public HashSet<CameraPixel> removeSmallCluster(HashSet<CameraPixel> showerPixel, int minNumberOfPixel)
//    {
//
//        ArrayList<Integer> list = new ArrayList<>();
//        for(CameraPixel pix : showerPixel){
//            list.add(pix.id);
//        }
//
//        ArrayList<ArrayList<Integer>> listOfLists = pixelMap.breadthFirstSearch(list);
//        ArrayList<Integer> newList = new ArrayList<>();
//        for (ArrayList<Integer> l: listOfLists){
//            if(l.size() <= minNumberOfPixel){
//                newList.addAll(l);
//            }
//        }
//
//        for(Integer pix : newList){
//            showerPixel.remove(pixelMap.getPixelFromId(pix));
//        }
//
//        return showerPixel;
//    }



}
