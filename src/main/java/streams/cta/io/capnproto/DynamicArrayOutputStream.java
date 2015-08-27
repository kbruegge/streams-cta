package streams.cta.io.capnproto;

import org.capnproto.BufferedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alexey on 26/08/15.
 */
public class DynamicArrayOutputStream implements BufferedOutputStream {

    private static final int ADD_BUFFER = 8192 / 2;
    static Logger log = LoggerFactory.getLogger(DynamicArrayOutputStream.class);

    ByteBuffer buffer;
    int bufferSize = 0;

    public DynamicArrayOutputStream() {
        bufferSize = 8192;
        buffer = ByteBuffer.allocate(bufferSize);
    }

    public DynamicArrayOutputStream(int bufferSize) {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public ByteBuffer getWriteBuffer() {
        return buffer;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int bufRemaining = this.buffer.remaining();
        int srcRemaining = src.remaining();
        if (bufRemaining < srcRemaining) {
            int biggerSize = bufferSize + (srcRemaining > ADD_BUFFER ? srcRemaining + ADD_BUFFER : ADD_BUFFER);
            ByteBuffer biggerBuffer = ByteBuffer.allocate(biggerSize);
            biggerBuffer.put(this.buffer.array());
            this.buffer = biggerBuffer;
            this.bufferSize = biggerSize;
        }
        this.buffer.put(src);
        return srcRemaining;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
