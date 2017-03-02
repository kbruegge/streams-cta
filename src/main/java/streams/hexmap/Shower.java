package streams.hexmap;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.HashSet;

/**
 * This class holds information about the shower. The set of selected signal pixels from the
 * telescopes image. The pixel class holds all required information to perform simple geometric
 * algorithms on the signal. Like the Hillas parameters for example. Each signal pixel also holds
 * the Ids of its neighbouring pixels. This is useful for dilating the shower.
 *
 * TODO: still missing is a lot of documentation
 *
 * Created by kaibrugge on 13.02.17.
 */
public class Shower implements Serializable {

    private final CameraMapping mapping = CameraMapping.getInstance();

    public final HashSet<Pixel> pixels = new HashSet<>();

    public final int cameraId;

    public Shower(int cameraId) {
        this.cameraId = cameraId;
    }

    public final static class Pixel {
        final public int cameraId;
        final public int pixelId;
        final public double weight;
        final public double xPositionInMM;
        final public double yPositionInMM;
        final public int[] neighbours;


        Pixel(int cameraId, int pixelId, double xPositionInM,
              double yPositionInM, double weight, int[] neighbours) {
            this.cameraId = cameraId;
            this.pixelId = pixelId;
            this.xPositionInMM = xPositionInM;
            this.yPositionInMM = yPositionInM;
            this.weight = weight;
            this.neighbours = neighbours;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pixel pixel = (Pixel) o;

            return cameraId == pixel.cameraId && pixelId == pixel.pixelId;
        }

        @Override
        public int hashCode() {
            int result = cameraId;
            result = 31 * result + pixelId;
            return result;
        }
    }

    private Pixel createPixel(int pixelId, double weight) {
        double x = mapping.cameraFromId(cameraId).pixelXPositions[pixelId];
        double y = mapping.cameraFromId(cameraId).pixelYPositions[pixelId];

        int[] neighbours = mapping.cameraFromId(cameraId).neighbours[pixelId];
        return new Pixel(cameraId, pixelId, x, y, weight, neighbours);

    }

    public void addPixel(int pixelId, double weight) {
        pixels.add(createPixel(pixelId, weight));
    }

    public void dilate(double[] image, double threshold) {
        HashSet<Pixel> ids = Sets.newHashSet();

        for (Pixel pix : pixels) {
            for (int n : pix.neighbours) {
                if (image[n] > threshold) {
                    ids.add(createPixel(n, image[n]));
                }
            }
        }

        pixels.addAll(ids);
    }

}
