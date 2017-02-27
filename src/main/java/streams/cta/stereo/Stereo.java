package streams.cta.stereo;

import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import stream.Data;
import stream.Processor;
import streams.hexmap.CameraMapping;
import streams.hexmap.TelescopeDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.*;

/**
 * The FitGammaHillas thing from Tino Michael. See https://github.com/tino-michael/ctapipe
 *
 *
 * Created by Kai on 20.02.17.
 */
public class Stereo  implements Processor{


    /**
     * Convert in-camera coordinates to direction vectors in 3D-space.
     *
     * @param x in-camera coordinates in meter
     * @param y in-camera coordinates in meter
     * @param phi pointing in radians
     * @param theta pointing in radians
     * @param foclen focal length of the telescope in radians
     * @param cameraRotation rotation of the camera within the telescope housing in radians
     * @return a direction vector, (x,y,z), corresponding to the direction of the given point in the camera
     */
    static double[] pixelDirection(double x, double y, double phi, double theta, double foclen, double cameraRotation){
        double alpha = atan2(y, x);
        double rho = length(x, y);
        double beta = rho / foclen;

        double[] telescopeDirection = cartesianFromPolar(phi, theta);

        double[] p = cartesianFromPolar(phi, theta + beta);

        return rotateAroundAxis(p, telescopeDirection, alpha - cameraRotation);

    }

    static double[] rotateAroundAxis(double[] vector, double[] axis, double angleInRadians){
        Vector3D v = new Vector3D(vector);
        Vector3D ax = new Vector3D(axis);

        Rotation rotation = new Rotation(ax, angleInRadians, RotationConvention.FRAME_TRANSFORM);
        Vector3D rotatedVector = rotation.applyTo(v);
        return rotatedVector.toArray();
    }

    /**
     * Go from spherical coordinates to cartesian coordinates. Useful for
     * creating a direction vector from the pointing of the telescope.
     * This assumes the typical 'mathematical conventions', or ISO, for naming these angles.
     * (radius r, inclination θ, azimuth φ)
     *
     * The converions works like this.
     *
     *    [ sin(theta)*cos(phi),
     *      sin(theta)*sin(phi),
     *      cos(theta)         ]
     *
     * @param phi pointing phi angle of a telescope
     * @param theta pointing theta angle of a telescope
     * @return a direction vector of length 1
     */
    static double[] cartesianFromPolar(double phi, double theta){
        double x = sin(theta) * cos(phi);
        double y = sin(theta) * sin(phi);
        double z = cos(theta);
        return new double[] {x, y, z};
    }

    private static double length(double x, double y){
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    private double length(double x, double y, double z){
        return sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
    }

    double phi(double x, double y){
        return atan2(y, x);
    }

    double theta(double x, double y, double z){
        double length = length(x, y, z);
        return acos(z / length);
    }

    @Override
    public Data process(Data data) {
        int[] triggeredTelescopes = (int[]) data.get("array:triggered_telescopes");

        List<Plane> planes = Arrays.stream(triggeredTelescopes)
                .mapToObj(id -> new Plane(id,0, 20, data))
                .collect(Collectors.toList());

        double[] direction = estimateDirection(planes);

        double[] corePosition = estimateCorePosition(planes);

        data.put("stereo:estimated_direction", direction);
        data.put("stereo:estimated_direction:x", direction[0]);
        data.put("stereo:estimated_direction:y", direction[1]);
        data.put("stereo:estimated_direction:z", direction[2]);

        data.put("stereo:estimated_impact_position", corePosition);
        data.put("stereo:estimated_direction:x", corePosition[0]);
        data.put("stereo:estimated_direction:y", corePosition[1]);

        return data;
    }


    double[] estimateCorePosition(List<Plane> planes){
        int n = planes.size();

        double[][] mat = new double[n][2];
        double[] d =  new double[n];

        for (int i = 0; i < n; i++) {
            Plane plane = planes.get(i);
            Vector3D norm = plane.getNormalAsVector();
            double weight = plane.weight;

            Vector2D projection = new Vector2D(norm.getX(), norm.getY());

            mat[i][0] = projection.getX() * weight;
            mat[i][1] = projection.getY() * weight;


            Vector2D telPos = new Vector2D(plane.telescopePosition[0], plane.telescopePosition[1]);

            d[i] = (projection.scalarMultiply(weight)).dotProduct(telPos);
        }

        //Do a linear least square regresssion
        RealMatrix A = MatrixUtils.createRealMatrix(mat);
        return MatrixUtils.inverse(A.transpose().multiply(A)).multiply(A.transpose()).operate(d);
    }

    double[] estimateDirection(List<Plane> planes){

        // get all permutations of size 2 in this (rather inelegant way)
        List<List<Plane>> tuples = new ArrayList<>();
        for(Plane p1 : planes){
            for(Plane p2 : planes){
                if(p1 != p2){
                    tuples.add(Lists.newArrayList(p1, p2));
                }
            }
        }

        Optional<Vector3D> direction = tuples.stream()
                .map(l -> {

                    Plane plane1 = l.get(0);
                    Plane plane2 = l.get(1);
                    double[] v1 = plane1.normalVector;
                    double[] v2 = plane2.normalVector;

                    Vector3D product = Vector3D.crossProduct(new Vector3D(v1), new Vector3D(v2));

                    //dont know what happens now. heres the sting from the python
                    // # two great circles cross each other twice (one would be
                    // # the origin, the other one the direction of the gamma) it
                    // # doesn't matter which we pick but it should at least be
                    // # consistent: make sure to always take the "upper"
                    // # solution

                    if (product.getZ() < 0) {
                        product = product.scalarMultiply(-1);
                    }
                    return product.scalarMultiply(plane1.weight * plane2.weight);

                })
                .reduce(Vector3D::add)
                .map(Vector3D::normalize);

        return direction.orElse(new Vector3D(0, 0, 0)).toArray();
    }

    static class Plane {
        public final int telescopeId;
        public final double weight;
        public final double[] v1;
        public final double[] v2;

        public final double[] normalVector;

        public final double[] telescopePosition;

        Plane(int id, double phi, double theta,  Data data) {
            this.telescopeId = id;
            double length = (double) data.get("telescope:" + id + ":shower:length");
            double width = (double) data.get("telescope:" + id + ":shower:width");
            double cogX = (double) data.get("telescope:" + id + ":shower:cog:x");
            double cogY = (double) data.get("telescope:" + id + ":shower:cog:y");
            double psi = (double) data.get("telescope:" + id + ":shower:psi");
            double size = (double) data.get("telescope:" + id + ":shower:size");

            TelescopeDefinition tel = CameraMapping.getInstance().telescopeFromId(id);

            //get two points on the shower axis
            double pX = cogX + length * cos(psi);
            double pY = cogY + length * sin(psi);

            double[] pDirection = pixelDirection(pX, pY, phi, theta, tel.opticalFocalLength, 0);
            double[] cogDirection = pixelDirection(cogX, cogY, phi, theta, tel.opticalFocalLength, 0);

            this.weight = size * (length / width);
            this.v1 = cogDirection;
            this.v2 = pDirection;

            // c  = (v1 X v2) X v1
            Vector3D c = Vector3D.crossProduct(Vector3D.crossProduct(new Vector3D(v1), new Vector3D(v2)), new Vector3D(v1));
            Vector3D norm = Vector3D.crossProduct(new Vector3D(v1), c);

            this.normalVector = norm.normalize().toArray();

            telescopePosition = new double[]{tel.telescopePositionX, tel.telescopePositionY, tel.telescopePositionZ};
        }

        Vector3D getNormalAsVector(){
            return new Vector3D(normalVector);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Plane plane = (Plane) o;
            return telescopeId == plane.telescopeId;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(telescopeId);
        }
    }
}
