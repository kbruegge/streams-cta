package streams.cta.cleaning;

import stream.Data;
import stream.annotations.Parameter;
import streams.cta.CTAExtractedDataProcessor;
import streams.cta.CTATelescope;
import streams.hexmap.LSTHexPixelMapping;
import streams.hexmap.CameraPixel;
import streams.hexmap.ui.overlays.PixelSetOverlay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

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
public class TwoLevelTimeNeighbor extends CTAExtractedDataProcessor {

    @Parameter(required = false)
    Double[] levels = {220., 120., 5., 1.};

    HashSet<CameraPixel> showerPixel = new HashSet<>();

    LSTHexPixelMapping pixelMap = LSTHexPixelMapping.getInstance();

    @Override
    public Data process(Data input, CTATelescope telescope, LocalDateTime timeStamp, double[] photons, double[] arrivalTimes) {
        showerPixel.clear();

        showerPixel = addCorePixel(showerPixel,photons,levels[0]);
        if(showerPixel.size() == 0 ){ return input; }

//        showerPixel = removeSmallCluster(showerPixel, 1);
//        if(showerPixel.size() == 0 ){ return input; }

        //showerPixel = addNeighboringPixels(showerPixel, photons, levels[1]);
//        showerPixel = applyTimeNeighborCleaning(showerPixel,arrivalTimes, levels[2], 1);
        //showerPixel = removeSmallCluster(showerPixel, levels[3].intValue());
//        showerPixel = applyTimeNeighborCleaning(showerPixel,arrivalTimes, levels[2], 1);



//        for (int i = 0; i < photons.length; i++) {
//            if (photons[i] > levels[0]){
//                showerPixel.add(pixelMap.getPixelFromId(i));
//            }
//        }

        if(showerPixel.size() < 5 ){
            return input;
        }

        input.put("shower", showerPixel);
        input.put("@showerOverlay",new PixelSetOverlay(showerPixel));

        //indicate outer pixel for testing purposes
        HashSet<CameraPixel> borderPixel = new HashSet<>();
        CameraPixel first = pixelMap.getPixelFromId(0);
        ArrayList<CameraPixel> neighbors = pixelMap.getNeighboursForPixel(first);
        borderPixel.addAll(neighbors);
//        for(CameraPixel p : pixelMap.getAllPixel()){
//            ArrayList<CameraPixel> neighbors = pixelMap.getNeighboursForPixel(p);
//            System.out.println(neighbors.size());
//        }
        input.put("@borderOverlay",new PixelSetOverlay(borderPixel));
        return input;
    }

    public void setLevels(Double[] levels) {
        this.levels = levels;
    }

    /**
     * Add all pixel with a weight > corePixelThreshold to the showerpixel list.
     * @param showerPixel 'HashSet containing the so far identified shower pixels'
     * @param photons
     * @param corePixelThreshold
     * @return
     */
    public HashSet<CameraPixel> addCorePixel(HashSet<CameraPixel> showerPixel, double[] photons, double corePixelThreshold) {
        for(int pixel = 0; pixel < photons.length; pixel++)
        {
            if (photons[pixel] > corePixelThreshold){
                showerPixel.add(pixelMap.getPixelFromId(pixel));
            }
        }
        return showerPixel;
    }

    /**
     * add all neighboring pixels of the core pixels, with a weight > neighborPixelThreshold to the showerpixellist
     * @param showerPixel 'HashSet containing the so far identified shower pixels'
     * @param photons
     * @return
     */
    public HashSet<CameraPixel> addNeighboringPixels(HashSet<CameraPixel> showerPixel, double[] photons, double neighborPixelThreshold)
    {
        HashSet<CameraPixel> newList = new HashSet<>();
        for (CameraPixel pix : showerPixel){
            ArrayList<CameraPixel> currentNeighbors = pixelMap.getNeighboursForPixel(pix);

            currentNeighbors.stream().filter(nPix -> photons[nPix.id] > neighborPixelThreshold && !newList.contains(nPix.id) && !showerPixel.contains(nPix.id)).forEach(nPix -> {
                newList.add(pixelMap.getPixelFromId(nPix.id));
            });
        }
        showerPixel.addAll(newList);
        return showerPixel;
    }


    /**
     * Remove all clusters of pixels with less than minNumberOfPixel pixels in the cluster
     * @param showerPixel 'HashSet containing the so far identified shower pixels'
     * @param minNumberOfPixel
     * @return
     */
    public HashSet<CameraPixel> removeSmallCluster(HashSet<CameraPixel> showerPixel, int minNumberOfPixel)
    {

        ArrayList<Integer> list = new ArrayList<>();
        for(CameraPixel pix : showerPixel){
            list.add(pix.id);
        }

        ArrayList<ArrayList<Integer>> listOfLists = pixelMap.breadthFirstSearch(list);
        ArrayList<Integer> newList = new ArrayList<>();
        for (ArrayList<Integer> l: listOfLists){
            if(l.size() <= minNumberOfPixel){
                newList.addAll(l);
            }
        }

        for(Integer pix : newList){
            showerPixel.remove(pixelMap.getPixelFromId(pix));
        }
        if(showerPixel.size() >= 5){
            System.out.println("tadaa");
        }
        return showerPixel;
    }

    /**
     * Remove pixels with less than minNumberOfNeighborPixel neighboring shower pixel,
     * which arrival time differs more than the timeThreshold from the current pixel
     * @param showerPixel 'HashSet containing the so far identified shower pixels'
     * @param arrivalTimes
     * @param timeThreshold
     * @param minNumberOfNeighborPixel
     * @return
     */
//    public HashSet<CameraPixel> applyTimeNeighborCleaning(HashSet<CameraPixel> showerPixel,double[] arrivalTimes, double timeThreshold, int minNumberOfNeighborPixel) {
//
//
//        HashSet<CameraPixel> newList= new HashSet<CameraPixel>();
//        for(CameraPixel pixel: showerPixel){
//            ArrayList<CameraPixel> currentNeighbors = pixelMap.getNeighboursForPixel(pixel);
//            int counter = 0;
//            double time = arrivalTimes[pixel.id];
//
//            for (CameraPixel nPix:currentNeighbors){
//                if( Math.abs(arrivalTimes[nPix.id]-time) < timeThreshold){
//                    counter++;
//                }
//            }
//            if (counter >= minNumberOfNeighborPixel)
//            {
//                newList.add(pixel);
//            }
//        }
//        return newList;
//    }


}
