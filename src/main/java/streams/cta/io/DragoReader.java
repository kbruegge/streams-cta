package streams.cta.io;

import scala.Int;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * A stream for test data from the drago module
 * Created by Kai on 13.01.16.
 */
public class DragoReader extends AbstractStream{

    private DataInputStream dataStream;

    private Data headerItem = DataFactory.create();

    public class Header {
        int event_id;
        int trigger_counter;
        long clock;
        byte[] flag = new byte[16];
        short[] stopcells = new short[8];

        @Override
        public String toString() {
            return "Header{" +
                    "event_id=" + event_id +
                    ", trigger_counter=" + trigger_counter +
                    ", clock=" + clock +
                    ", flag=" + Arrays.toString(flag) +
                    ", stopcells=" + Arrays.toString(stopcells) +
                    '}';
        }
    }

    public DragoReader(SourceURL url) {
        super(url);
    }

    @Override
    public Data readNext() throws Exception {
        Data item = DataFactory.create(headerItem);


        Header header = readHeader(dataStream);
        System.out.println(header.toString());


        int roi = 1024;
        int numChannels = 8;
        int numGains = 2;

        byte[] data = new byte[numGains * numChannels * roi * 2];
        int numBytes = dataStream.read(data);

        ShortBuffer sBuf = ByteBuffer.wrap(data).asShortBuffer();
        short[] ar = new short[numGains * numChannels * roi];
        sBuf.get(ar);

        System.out.println(Arrays.toString(ar));

        return null;
    }

    @Override
    public void init() throws Exception {
        super.init();
        BufferedInputStream bStream = new BufferedInputStream(url.openStream(),
                1024 +  128);
        dataStream = new DataInputStream(bStream);

    }

    private Header readHeader(DataInputStream dataStream) throws IOException {
        Header header = new Header();
        header.event_id = dataStream.readInt();
        header.trigger_counter = dataStream.readInt();
        header.clock = dataStream.readLong();
        dataStream.read(header.flag);

        for (int stopcell = 0; stopcell < 8; stopcell++) {
            header.stopcells[stopcell] = dataStream.readShort();
        }

        return header;
    }
}
