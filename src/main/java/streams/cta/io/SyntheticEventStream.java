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

    int numberOfPixels = 1855;
    int numberOfSlices = 30;

    Random random = new Random();
    byte[] randomBytes;
	Random random = new Random();
    short[][] data;

    int counter;
    @Parameter(required = false)
    int delay = 0;


    long eventId = 1;
    int telescopeId = 0;
    int[] brokenPixel = new int[]{2, 12, 333};
    double[] gains = new double[numberOfPixels];
    CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, telescopeId, 0, 0, 0, brokenPixel, gains, gains);

    public double f(int x) {
        return 100 * Math.exp(-0.04 * Math.pow((x - (10 + 4 * (random.nextDouble() - 0.5))), 2));
    }

    /**
     * @see stream.io.AbstractStream#init()
     */
    @Override
    public void init() throws Exception {
        super.init();
    }

    /**
     * @see stream.io.AbstractStream#readNext()
     */
    @Override
    public Data readNext() throws Exception {
        if (counter++>3000){
            System.out.println("sleep");
            Thread.sleep(20000);
            counter = 0;
        }

        data = new short[numberOfPixels][numberOfSlices];

        for (int pixel = 0; pixel < numberOfPixels; pixel++) {
            for (int x = 0; x < numberOfSlices; x++) {
                //data[pixel][x] = (short) f(x);
                data[pixel][x] += random.nextGaussian();
            }
//            randomBytes = new byte[numberOfSlices*2];
//            random.nextBytes(randomBytes);
//            ByteBuffer.wrap(randomBytes).asShortBuffer().get(data[pixel]);
        }
        try {
            Thread.sleep(delay);
        } catch(InterruptedException ex) {
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
