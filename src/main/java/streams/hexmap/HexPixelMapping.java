/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;

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


    public abstract int getNumberOfPixel();

    public abstract T getPixelFromId(int id);
    public abstract T getPixelFromOffsetCoordinates(int x, int y);


    public ArrayList<T> getNeighboursForPixelId(int pixelId) {
        return getNeighboursForPixel(getPixelFromId(pixelId));
    }

    public ArrayList<T> getNeighboursForPixel(CameraPixel p) {
        ArrayList<T> l = new ArrayList<>();
        //check if x coordinate is even or not
        int parity = (p.geometricX & 1);
        //get the neighbour in each direction and store them in hte list
        for (int direction = 0; direction <= 5; direction++) {
            int[] d = neighbourOffsets[parity][direction];
            T np = getPixelFromOffsetCoordinates(p.geometricX + d[0], p.geometricY + d[1]);
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
     * @return The pixel below the point or NULL if the pixels does not exist.
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
}
