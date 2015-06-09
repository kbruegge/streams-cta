/**
 * 
 */
package streams.cta.io;

import java.nio.ByteBuffer;
import java.util.Random;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import streams.cta.CTAEvent;

/**
 * @author chris
 * 
 */
public class SyntheticEventStream extends AbstractStream {

	int numberOfPixels = 20;
	int numberOfSlices = 60;

	Random random = new Random();
    byte[] randomBytes;
    short[] data;

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
        randomBytes = new byte[numberOfPixels*numberOfSlices*2];
        data = new short[numberOfPixels*numberOfSlices];
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {
        random.nextBytes(randomBytes);
        ByteBuffer.wrap(randomBytes).asShortBuffer().get(data);

		CTAEvent evt = new CTAEvent(numberOfPixels, data);
		Data item = DataFactory.create();
		item.put("@event", evt);
		return item;
	}
}
