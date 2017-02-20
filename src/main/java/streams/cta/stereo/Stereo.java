package streams.cta.stereo;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import stream.Data;
import stream.Processor;

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
        for(int id : triggeredTelescopes){
            double length = (double) data.get("telescope:" + id + ":shower:length");
            double cogX = (double) data.get("telescope:" + id + ":shower:cog:x");
            double cogY = (double) data.get("telescope:" + id + ":shower:cog:y");
            double psi = (double) data.get("telescope:" + id + ":shower:psi");

            //get two points on the shower axis
            double pX = cogX + length*cos(psi);
            double pY = cogY + length*sin(psi);

            double[] pDirection = pixelDirection(pX, pY, 0, 20, 15, 0);
            double[] cogDirection = pixelDirection(cogX, cogY, 0, 20, 15, 0);

        }
        return null;
    }
    
    class Plane {
        public final int telescopeId;
        public final double weight;
        public final double[] v1;
        public final double[] v2;

        public final double[] normalVector;

        Plane(int telescopeId, double weight, double[] v1, double[] v2) {
            this.telescopeId = telescopeId;
            this.weight = weight;
            this.v1 = v1;
            this.v2 = v2;

            Vector3D c = Vector3D.crossProduct(new Vector3D(v1), new Vector3D(v2));
            Vector3D norm = Vector3D.crossProduct(new Vector3D(v1), c);

            this.normalVector = norm.normalize().toArray();
        }
    }
}
