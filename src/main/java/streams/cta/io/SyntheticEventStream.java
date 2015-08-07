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
            randomBytes = new byte[numberOfSlices*2];
            random.nextBytes(randomBytes);
            ByteBuffer.wrap(randomBytes).asShortBuffer().get(data[pixel]);
        }

		TelescopeEvent evt = new TelescopeEvent(eventId, data, LocalDateTime.now());
		Data item = DataFactory.create();
		item.put("@event", evt);
		item.put("@source", this.getClass().getSimpleName());

        eventId++;
		return item;
	}
}
