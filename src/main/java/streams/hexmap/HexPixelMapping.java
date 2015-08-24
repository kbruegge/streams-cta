/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * This class provides a mapping between different Pixel ids and geometric information from the
 * camera layout.
 *
 * This class can get instantiated as a singleton with the getInstance() method.
 *
 * The geometric coordinates stored in the text file to build this map are stored in the "odd -q" vertical layout
 * See http://www.redblobgames.com/grids/hexagons/ for details and pictures.
 *
 * @author kai
 */
public abstract class HexPixelMapping<T extends CameraPixel> {

    static Logger log = LoggerFactory.getLogger(HexPixelMapping.class);

    //these offsets are a universal property of hexagonal coordinates.
    private final int[][][] neighbourOffsets = {
            {{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}}, //uneven
            {{1, 1}, {1, 0}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}  //pixel with a even x coordinate
    };


    private T[] pixelArray;

    private T[][] offsetCoordinates;
    private int cameraMaxOffsetX;
    private int cameraMaxOffsetY;
    private int cameraMinOffsetX;
    private int cameraMinOffsetY;

    public abstract int getNumberOfPixel();

    protected abstract T getPixelFromCSVItem(Data item);


    public T getPixelFromOffsetCoordinates(int x, int y){
        if (x + cameraMaxOffsetX >= offsetCoordinates.length || y + cameraMaxOffsetY >= offsetCoordinates[0].length){
            return null;
        }
        if (x + cameraMaxOffsetX < 0 || y + cameraMaxOffsetY < 0){
            return null;
        }
        return offsetCoordinates[x + cameraMaxOffsetX][y + cameraMaxOffsetY];
    }

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

//        FIXME: test this. See Effective Java Item 26 on how to create generic arrays. this could be replaced by some hashmap of course.
        //But I cant think of a quick hash function.
//        ArrayList<T> l = new ArrayList<>();
        pixelArray = (T[]) new CameraPixel[getNumberOfPixel()];

        for (int i = 0; i < getNumberOfPixel(); i++) {
            Data item = null;
            try {
                item = stream.readNext();
            } catch (Exception e) {
                log.error(e.toString());
            }
            T p = getPixelFromCSVItem(item);
            pixelArray[i] = p;
        }

        cameraMaxOffsetX = Arrays.stream(pixelArray).map(e -> Math.abs(e.offsetCoordinateX)).max(Integer::compare).get();
        cameraMinOffsetX = Arrays.stream(pixelArray).map(e -> Math.abs(e.offsetCoordinateX)).min(Integer::compare).get();
        cameraMaxOffsetY = Arrays.stream(pixelArray).map(e -> Math.abs(e.offsetCoordinateY)).max( Integer::compare).get();
        cameraMinOffsetY = Arrays.stream(pixelArray).map(e -> Math.abs(e.offsetCoordinateY)).min( Integer::compare).get();
        offsetCoordinates = (T[][]) new CameraPixel[cameraMaxOffsetX*2 + 1][cameraMaxOffsetY*2 + 1];
        for (T pixel : pixelArray){
            offsetCoordinates[pixel.offsetCoordinateX + cameraMaxOffsetX][pixel.offsetCoordinateY + cameraMaxOffsetY] = pixel;
        }
    }

    public ArrayList<T> getNeighboursForPixel(CameraPixel p) {
        ArrayList<T> l = new ArrayList<>();
        //check if x coordinate is even or not
        int parity = (p.offsetCoordinateX & 1);
        //get the neighbour in each direction and store them in the list
        for (int direction = 0; direction <= 5; direction++) {
            int[] d = neighbourOffsets[parity][direction];
            T np = getPixelFromOffsetCoordinates(p.offsetCoordinateX + d[0], p.offsetCoordinateY + d[1]);
            if (np != null){
                l.add(np);
            }
        }
        return l;
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

        //if (xCoordinate*xCoordinate + yCoordinate*yCoordinate >= 440){
        //    return null;
        //}
        //distance from center to corner
        double size  = 1.0/Math.sqrt(3);

        double axial_q = 2.0/3.0 * xCoordinate/size;
        double axial_r = (0.5773502693 * yCoordinate - 1.0/3.0 *xCoordinate)/size;


        double cube_x = axial_q;
        double cube_z = axial_r;
        double cube_y = -cube_x-cube_z;


        //now round maybe violating the constraint
        int rx = (int) Math.round(cube_x);
        int rz = (int) Math.round(cube_z);
        int ry = (int) Math.round(cube_y);

        //artificially fix the constraint.
        double x_diff = Math.abs(rx -cube_x);
        double z_diff = Math.abs(rz -cube_z);
        double y_diff = Math.abs(ry -cube_y);

        if(x_diff > y_diff && x_diff > z_diff){
            rx = -ry-rz;
        } else if(y_diff > z_diff){
            ry = -rx-rz;
        } else {
            rz = -rx-ry;
        }


        //now convert cube coordinates back to even-q
        int qd = rx;
        int rd = rz + (rx - (rx&1))/2;

        T p = getPixelFromOffsetCoordinates(qd, rd);
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
    public ArrayList<ArrayList<Integer>> breadthFirstSearch(List<Integer> showerPixel) {
        ArrayList<ArrayList<Integer>> listOfIslands = new ArrayList<>();
        HashSet<Integer> marked = new HashSet<>();

        for (int pix : showerPixel) {
            if (!marked.contains(pix)) {
                // start BFS
                marked.add(pix);
                ArrayList<Integer> q = new ArrayList<Integer>();
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
