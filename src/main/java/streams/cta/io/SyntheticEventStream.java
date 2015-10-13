/**
 *
 */
package streams.cta.io;

import java.time.LocalDateTime;
import java.util.Random;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * @author chris, kai
 */
public class SyntheticEventStream extends AbstractStream {
    
    @Parameter(required = false)
    int delay = 0;

    int numberOfPixels;
    int numberOfSlices;

    Random random;
    short[][] data;

    long eventId = 1;
    int telescopeId = 0;
    int[] brokenPixel;
    double[] gains;
    CTATelescope telescope;

    /**
     * Helper function to produce a random value.
     */
    //TODO is not used anymore.
    public double f(int x) {
        return 100 * Math.exp(-0.04 * Math.pow((x - (10 + 4 * (random.nextDouble() - 0.5))), 2));
    }

    /**
     * @see stream.io.AbstractStream#init()
     */
    @Override
    public void init() throws Exception {
        super.init();
        random = new Random();
        numberOfPixels = 1855;
        numberOfSlices = 30;
        brokenPixel = new int[]{2, 12, 333};
        gains = new double[numberOfPixels];
        telescope = new CTATelescope(
                CTATelescopeType.LST, telescopeId, 0, 0, 0, brokenPixel, gains, gains);
    }

    /**
     * @see stream.io.AbstractStream#readNext()
     */
    @Override
    public Data readNext() throws Exception {
        data = new short[numberOfPixels][numberOfSlices];

        for (int pixel = 0; pixel < numberOfPixels; pixel++) {
            for (int x = 0; x < numberOfSlices; x++) {
                //data[pixel][x] = (short) f(x);
                data[pixel][x] += random.nextGaussian();
            }
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        Data item = DataFactory.create();
        item.put("@raw_data", data);
        item.put("@telescope", telescope);
        item.put("@timestamp", LocalDateTime.now());
        item.put("@source", this.getClass().getSimpleName());

        eventId++;
        return item;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
