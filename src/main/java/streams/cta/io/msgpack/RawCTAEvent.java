package streams.cta.io.msgpack;

import org.msgpack.annotation.Message;

/**
 * Created by alexey on 20/08/15.
 */
@Message
public class RawCTAEvent {
    public String messageType;

    public int telescopeId;

    public int roi;

    public int numPixel;

    public short[] samples;
}
