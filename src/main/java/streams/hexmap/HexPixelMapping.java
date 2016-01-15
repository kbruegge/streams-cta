/**
 * 
 */
package streams.hexmap;

import org.apache.storm.guava.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;

/**
 * This class provides a mapping between different Pixel ids and geometric information from the
 * camera layout.
 *
 * This class can get instantiated as a singleton with the getInstance() method.
 *
 * The geometric coordinates stored in the text file to build this default map are stored in the "odd -q" vertical layout.
 * Other PixelMappings can be stored
 * See http://www.redblobgames.com/grids/hexagons/ for details and pictures.
 *
 * @author kai
 */
public abstract class HexPixelMapping<T extends CameraPixel> {

    static Logger log = LoggerFactory.getLogger(HexPixelMapping.class);

    public enum Orientation {
        FLAT_TOP(0),
        POINTY_TOP(1);

        private final int orientation;

        Orientation(int i) {
            this.orientation = i;
        }
    }


    protected Orientation orientation = Orientation.FLAT_TOP;

    //radius of a pixel. flat to flat.
    protected final double pixelRadius = 10.0;

    private final int[][] neighbourOffsets = {
            {+1, 0}, {+1, -1}, {0, -1}, {-1, 0}, {-1, +1}, {0, +1}
    };


    private T[] pixelArray;


    //This array contains the camera pixels in axial layout. I dont care about unused entries.
    private T[][] axialGrid;
    int qOffset = 0;
    int rOffset = 0;


//    private int cameraMinOffsetX;
//    private int cameraMinOffsetY;

    public abstract int getNumberOfPixel();

    protected abstract T getPixelFromCSVItem(Data item);

//
//    public T getPixelFromAxialCoordinates(int x, int y){
////        if (x + cameraMaxOffsetX >= offsetCoordinates.length || y + cameraMaxOffsetY >= offsetCoordinates[0].length){
////            return null;
////        }
////        if (x + cameraMaxOffsetX < 0 || y + cameraMaxOffsetY < 0){
////            return null;
////        }
////        return offsetCoordinates[x + cameraMaxOffsetX][y + cameraMaxOffsetY];
//        return null;
//    }

    public ArrayList<T> getNeighboursForPixelId(int pixelId) {
        return getNeighboursForPixel(getPixelFromId(pixelId));
    }


    public T getPixelFromId(int id){
        return pixelArray[id];
    }


    /**
     * This expects a file containing information on all of the pixel
     * @param mapping url to the mapping file
     */

    protected void load(URL mapping){
        //use the csv stream to read stuff from the csv file
        CsvStream stream = null;
        try {
            stream = new CsvStream(new SourceURL(mapping), ",");
            stream.init();
        } catch (Exception e){
            log.error(e.toString());
        }

//        TODO: test this. See Effective Java Item 26 on how to create generic arrays. this could be replaced by some hashmap of course.
        //But I cant think of a quick hash function.
//        ArrayList<T> l = new ArrayList<>();
        pixelArray = (T[]) new CameraPixel[getNumberOfPixel()];


        int maxQ = Integer.MIN_VALUE;
        int minQ = Integer.MAX_VALUE;

        int maxR = Integer.MIN_VALUE;
        int minR = Integer.MAX_VALUE;

        for (int i = 0; i < getNumberOfPixel(); i++) {
            Data item = null;
            try {
                item = stream.readNext();
            } catch (Exception e) {
                log.error(e.toString());
            }
            T p = getPixelFromCSVItem(item);

            maxQ = Math.max(maxQ, p.axialQ);
            minQ = Math.min(minQ, p.axialQ);

            maxR = Math.max(maxR, p.axialR);
            minR = Math.min(minR, p.axialR);

            pixelArray[i] = p;
        }
        qOffset = abs(minQ);
        rOffset = abs(minR);

        axialGrid = (T[][]) new CameraPixel[abs(minQ) + maxQ + 1][abs(minR) + maxR + 1];

        for (T pixel : pixelArray){
            axialGrid[pixel.axialQ + abs(minQ)][pixel.axialR + abs(minR)] = pixel;
        }
    }

    private T getPixelFromAxialCoordinates(int q, int r) {
        if(q < 0 || r < 0 ||(q + qOffset) >= axialGrid.length || (r + rOffset) >= axialGrid[0].length) {
            return null;
        }

        return axialGrid[q + qOffset][r + rOffset];
    }

    public ArrayList<T> getNeighboursForPixel(CameraPixel p) {
        ArrayList<T> l = new ArrayList<>();
        //check if x coordinate is even or not
        //int parity = (p.offsetCoordinateX & 1);
        //get the neighbour in each direction and store them in the list
        for (int direction = 0; direction <= 5; direction++) {
            int[] d = neighbourOffsets[direction];
            T np = getPixelFromAxialCoordinates(p.axialQ + d[0], p.axialR + d[1]);
            if (np != null){
                l.add(np);
            }
        }
        return l;
    }

    public ArrayList<T> getAllPixel(){
        return Lists.newArrayList(pixelArray);
    }

    protected int[] getAxialCoordinatesFromRealWorlCoordinates(double x, double y){
        return null;
    }

    /**
     * Get the FactCameraPixel sitting below the coordinates passed to the method.
     * The center of the coordinate system in the camera is the center of the camera.
     *
     * @param xCoordinate
     * @param yCoordinate
     * @return The pixel below the point or NULL if the pixel does not exist.
     */
    public T getPixelBelowCoordinatesInMM(double xCoordinate, double yCoordinate){
        //get some pixel near the point provided
        //in pixel units
        xCoordinate /= 9.5;
        yCoordinate /= -9.5;
        yCoordinate += 0.5;

        //distance from center to corner
        double size  = 1.0/Math.sqrt(3);
        double axial_r = 0;
        double axial_q = 0;

        if (orientation == Orientation.FLAT_TOP) {
            axial_q = 2.0 / 3.0 * xCoordinate / size;
            axial_r = (0.5773502693 * yCoordinate - xCoordinate/3.0) / size;
        } else if (orientation == Orientation.POINTY_TOP) {
            axial_q = (0.5773502693 * xCoordinate - yCoordinate/3.0) / size;
            axial_r = 2.0 / 3.0 * yCoordinate / size;
        }


        double cube_x = axial_q;
        double cube_z = axial_r;
        double cube_y = -cube_x-cube_z;


        //now round maybe violating the constraint
        int rx = (int) Math.round(cube_x);
        int rz = (int) Math.round(cube_z);
        int ry = (int) Math.round(cube_y);

        //artificially fix the constraint.
        double x_diff = abs(rx -cube_x);
        double z_diff = abs(rz -cube_z);
        double y_diff = abs(ry -cube_y);

        if(x_diff > y_diff && x_diff > z_diff){
            rx = -ry-rz;
        } else if(y_diff > z_diff){
            ry = -rx-rz;
        } else {
            rz = -rx-ry;
        }

        //convert the cube coordinate back to axial coordiantes
        int q = rx;
        int r = rz;

        T p = getPixelFromAxialCoordinates(q, r);
        //now convert cube coordinates back to even-q
        int qd = rx;
        int rd = rz + (rx - (rx&1))/2;

        p = getPixelFromAxialCoordinates(qd, rd);
        return p;
    }



    /**
     * Finds all unconnected sets of pixel in the showerPixel List and returns a
     * list of lists. Each list containing one separate set. Does a BFs search.
     * See the wikipedia article on BFS. This version is not as memory efficient
     * as it could be.
     *
     * @param showerPixel
     *            the list to search in
     * @return A list of lists.
     */
    public ArrayList<ArrayList<Integer>> breadthFirstSearch(Collection<Integer> showerPixel) {
        ArrayList<ArrayList<Integer>> listOfIslands = new ArrayList<>();
        HashSet<Integer> marked = new HashSet<>();

        for (int pix : showerPixel) {
            if (!marked.contains(pix)) {
                // start BFS
                marked.add(pix);
                ArrayList<Integer> q = new ArrayList<>();
                q.add(pix);
                for (int index = 0; index < q.size() && !q.isEmpty(); index++) {
                    // add neighbours to q
                    ArrayList<T> neighbors = getNeighboursForPixelId(q.get(index));
                    for (T i : neighbors) {
                        if (showerPixel.contains(i.id)
                                && !marked.contains(i.id)) {
                            q.add(i.id);
                            marked.add(i.id);
                        }
                    }
                }
                listOfIslands.add(q);
            }
        }
        return listOfIslands;
    }
}
