/**
 * 
 */
package streams.cta.io;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Random;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.TelescopeEvent;

/**
 * @author chris
 * 
 */
public class SyntheticEventStream extends AbstractStream {

	int numberOfPixels = 1800;
	int numberOfSlices = 30;

	Random random = new Random();
    byte[] randomBytes;
    short[][] data;


    long eventId = 1;

	public double f(int x){
        return 100*Math.exp(-0.04*Math.pow((x - 10), 2));
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
        data = new short[numberOfPixels][numberOfSlices];


        for (int pixel = 0; pixel < numberOfPixels; pixel++) {
            for (int x = 0; x < numberOfSlices; x++) {
                data[pixel][x] = (short) f(x);
                data[pixel][x] += 10*random.nextGaussian();
            }
//            randomBytes = new byte[numberOfSlices*2];
//            random.nextBytes(randomBytes);
//            ByteBuffer.wrap(randomBytes).asShortBuffer().get(data[pixel]);
        }

		TelescopeEvent evt = new TelescopeEvent(eventId, data, LocalDateTime.now());
		Data item = DataFactory.create();
		item.put("@event", evt);
		item.put("@source", this.getClass().getSimpleName());

        eventId++;
		return item;
	}
}
