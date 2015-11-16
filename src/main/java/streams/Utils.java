package streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;

import stream.Data;

/**
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class Utils {

    public static double[] floatToDoubleArray(float[] ar) {
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] intToDoubleArray(int[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] shortToDoubleArray(short[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] byteToDoubleArray(byte[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }


    /**
     * This is method might be useful for getting stuff from the data items and, if possible, cast
     * it into a double array. This is not very fast. And its also very ugly. But thats okay. I
     * still like you
     *
     * @return can be null!
     */
    public static double[] toDoubleArray(Serializable arr) {
        if (arr.getClass().isArray()) {
            Class<?> clazz = arr.getClass().getComponentType();
            if (clazz.equals(float.class)) {
                return floatToDoubleArray((float[]) arr);
            } else if (clazz.equals(double.class)) {
                return (double[]) arr;
            } else if (clazz.equals(int.class)) {
                return intToDoubleArray((int[]) arr);
            } else if (clazz.equals(short.class)) {
                return shortToDoubleArray((short[]) arr);
            } else if (clazz.equals(byte.class)) {
                return byteToDoubleArray((byte[]) arr);
            }
        }
        return null;
    }



}
