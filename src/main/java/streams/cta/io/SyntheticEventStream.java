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
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

/**
 * @author chris, kai
 * 
 */
public class SyntheticEventStream extends AbstractStream {

	int numberOfPixels = 1800;
	int numberOfSlices = 30;

	Random random = new Random();
    byte[] randomBytes;
    short[][] data;


    long eventId = 1;
	int telescopeId = 0;
	int[] brokenPixel = new int[]{2,12,333};
	double[] gains = new double[numberOfPixels];
	CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, telescopeId, 0 ,0 ,0, brokenPixel, gains, gains  );

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


		Data item = DataFactory.create();
		item.put("data", data);
		item.put("@telescope", telescope);
		item.put("@timestamp", LocalDateTime.now());
		item.put("@source", this.getClass().getSimpleName());

        eventId++;
		return item;
	}
}
