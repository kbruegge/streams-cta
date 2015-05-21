/**
 * 
 */
package streams.cta.io;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
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

	ByteBuffer randomBytes;
	ShortBuffer shorts;

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();

		byte[] bytes = new byte[numberOfPixels * numberOfSlices * 2];
		randomBytes = ByteBuffer.wrap(bytes);
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {

		short[] data = new short[numberOfPixels * numberOfSlices];

		for (int i = 0; i < data.length; i++) {
			data[i] = 0;
		}
		// random.nextBytes(randomBytes.array());
		// randomBytes.rewind();
		// shorts = randomBytes.asShortBuffer();
		// shorts.get(data, 0, data.length);

		CTAEvent evt = new CTAEvent(numberOfPixels, data);
		Data item = DataFactory.create();
		item.put("@event", evt);
		return item;
	}

	/**
	 * @return the numberOfPixels
	 */
	public int getNumberOfPixels() {
		return numberOfPixels;
	}

	/**
	 * @param numberOfPixels
	 *            the numberOfPixels to set
	 */
	public void setNumberOfPixels(int numberOfPixels) {
		this.numberOfPixels = numberOfPixels;
	}

	/**
	 * @return the numberOfSlices
	 */
	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	/**
	 * @param numberOfSlices
	 *            the numberOfSlices to set
	 */
	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}
}
