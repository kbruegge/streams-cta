package streams.cta.stereo;

import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
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


    double[] pixelDirection(double x, double y, double phi, double theta, double foclen, double cameraRotation){
        double alpha = atan2(y, x);
        double rho = length(x, y);
        double beta = rho / foclen;

        double[] telescopeDirection = directionVectorFromAngles(phi, theta);

        double[] p = directionVectorFromAngles(phi, theta + beta);

        return rotateAroundAxis(p, telescopeDirection, alpha - cameraRotation);

    }

    double[] rotateAroundAxis(double[] vector, double[] axis, double angle){
        Vector3D v = new Vector3D(vector);
        Vector3D ax = new Vector3D(axis);

        Rotation rotation = new Rotation(ax, angle, RotationConvention.FRAME_TRANSFORM);
        Vector3D rotatedVector = rotation.applyTo(v);
        return rotatedVector.toArray();
    }

    /**
     * Create a direction vector from the pointing of the telescope
     *    [ sin(theta)*cos(phi),
     *      sin(theta)*sin(phi),
     *      cos(theta)         ]
     * @param phi pointing phi angle of a telescope
     * @param theta pointing theta angle of a telescope
     * @return a direction vector of length 1 originating at the telescope
     */
    double[] directionVectorFromAngles(double phi, double theta){
        double x = sin(theta) * cos(theta);
        double y = sin(theta) * sin(theta);
        double z = cos(theta);
        return new double[] {x, y, z};
    }

    double length(double x, double y){
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    double length(double x, double y, double z){
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
                .mapToObj(id -> new Plane(id, data))
                .collect(Collectors.toList());

        Vector3D direction = estimateDirection(planes);

        double[] corePosition = estimateCorePosition(planes);


        return data;
    }


    public double[] estimateCorePosition(List<Plane> planes){
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

            d[i] = projection.dotProduct(telPos);
        }

        //Do a linear least square regresssion
        RealMatrix A = MatrixUtils.createRealMatrix(mat);
        return MatrixUtils.inverse(A.transpose().multiply(A)).multiply(A.transpose()).operate(d);
    }

    Vector3D estimateDirection(List<Plane> planes){

        // get all permutations of size 2 in the most inefficient way possible
        List<List<Plane>> tuples = Collections2.permutations(planes)
                .stream()
                .filter(l -> l.size() == 2)
                .filter(l -> !l.get(0).equals(l.get(1)))
                .collect(Collectors.toList());

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
                .reduce(Vector3D::add);

        return direction.orElse(new Vector3D(0, 0, 0)).normalize();
    }

    class Plane {
        public final int telescopeId;
        public final double weight;
        public final double[] v1;
        public final double[] v2;

        public final double[] normalVector;

        public final double[] telescopePosition;

        Plane(int id, Data data) {
            this.telescopeId = id;
            double length = (double) data.get("telescope:" + id + ":shower:length");
            double width = (double) data.get("telescope:" + id + ":shower:width");
            double cogX = (double) data.get("telescope:" + id + ":shower:cog:x");
            double cogY = (double) data.get("telescope:" + id + ":shower:cog:y");
            double psi = (double) data.get("telescope:" + id + ":shower:psi");
            double size = (double) data.get("telescope:" + id + ":shower:size");

            //get two points on the shower axis
            double pX = cogX + length*cos(psi);
            double pY = cogY + length*sin(psi);

            double[] pDirection = pixelDirection(pX, pY, 0, 20, 15, 0);
            double[] cogDirection = pixelDirection(cogX, cogY, 0, 20, 15, 0);

            //this.weight = weight;
            this.weight = size * (length / width);
            this.v1 = cogDirection;
            this.v2 = pDirection;

            Vector3D c = Vector3D.crossProduct(new Vector3D(v1), new Vector3D(v2));
            Vector3D norm = Vector3D.crossProduct(new Vector3D(v1), c);

            this.normalVector = norm.normalize().toArray();

            TelescopeDefinition tel = CameraMapping.getInstance().telescopeFromId(id);

            telescopePosition = new double[]{tel.telescopePositionX, tel.telescopePositionY, tel.telescopePositionZ};
        }

        public Vector3D getNormalAsVector(){
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
