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

	int numberOfPixels = 20;
	int numberOfSlices = 60;

	Random random = new Random();
    byte[] randomBytes;
    short[][] data;
    int[] pixelIds;

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

        numberOfPixels = random.nextInt(2200);
        data = new short[numberOfPixels][numberOfSlices];

        for (int pixel = 0; pixel < numberOfPixels; pixel++) {
            randomBytes = new byte[numberOfSlices*2];
            random.nextBytes(randomBytes);
            ByteBuffer.wrap(randomBytes).asShortBuffer().get(data[pixel]);
        }


        pixelIds = new int[numberOfPixels];
        int startId = random.nextInt(2200 - numberOfPixels);

        for (short i = 0; i < pixelIds.length; i++) {
            pixelIds[i] = startId + i;
        }

		TelescopeEvent evt = new TelescopeEvent(eventId, numberOfPixels, pixelIds, data, LocalDateTime.now());
		Data item = DataFactory.create();
		item.put("@event", evt);

        eventId++;
		return item;
	}
}
