package streams.cta.io.capnproto;

import org.apache.storm.netty.buffer.ChannelBuffer;
import org.apache.storm.netty.buffer.ChannelBuffers;
import org.capnproto.BufferedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alexey on 26/08/15.
 */
public class DynamicArrayOutputStream implements BufferedOutputStream {
    
    static Logger log = LoggerFactory.getLogger(DynamicArrayOutputStream.class);

    ChannelBuffer chBuffer;
    int bufferSize = 0;

    public DynamicArrayOutputStream() {
        bufferSize = 70000;
        chBuffer = ChannelBuffers.dynamicBuffer(bufferSize);
    }

    public DynamicArrayOutputStream(int bufferSize) {
        this.chBuffer = ChannelBuffers.dynamicBuffer(bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public ByteBuffer getWriteBuffer() {
        return chBuffer.toByteBuffer();
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int srcRemaining = src.remaining();
        this.chBuffer.writeBytes(src);
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
