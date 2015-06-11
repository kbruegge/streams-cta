/**
 * 
 */
package streams.cta;

import java.io.Serializable;

/**
 * @author chris
 * 
 */
public class CTAEvent implements Serializable {

	/** The unique class ID */
	private static final long serialVersionUID = -595319426051864811L;

	protected final int numberOfPixels;
	protected final byte[] data;

	public CTAEvent(int numPixels, byte[] data) {
		this.numberOfPixels = numPixels;
		this.data = data;
	}

	public int numberOfPixels() {
		return numberOfPixels;
	}

	public byte[] values() {
		return data;
	}
}
