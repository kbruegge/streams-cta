package streams.cta.io.capnproto;


import org.capnproto.BufferedInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alexey on 26/08/15.
 */
public class DynamicArrayInputStream implements BufferedInputStream {

    ByteBuffer buffer;

    public DynamicArrayInputStream(ByteBuffer buffer) {
        this.buffer = buffer.asReadOnlyBuffer();
    }

    @Override
    public ByteBuffer getReadBuffer() throws IOException {
        return this.buffer;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int bufferRemaining = this.buffer.remaining();
        int dstRemaining = dst.remaining();
        ByteBuffer bufferSlice = this.buffer.slice();
        int limit = dstRemaining < bufferRemaining ? dstRemaining : bufferRemaining;
        bufferSlice.limit(limit);
        dst.put(bufferSlice);
        this.buffer.position(this.buffer.position() + limit);
        return dstRemaining;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
