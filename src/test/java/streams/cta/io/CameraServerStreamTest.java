package streams.cta.io;

import org.junit.Assert;
import org.junit.Test;
import stream.Data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Kai on 16.11.15.
 */
public class CameraServerStreamTest {

    @Test
    public void test() throws Exception {
        CameraServerStream stream = new CameraServerStream();
        stream.addresses = new String[]{"tcp://127.0.0.1:4849"};
        stream.init();
        Data item = stream.readNext();
        assertThat(item, is(notNullValue()));
    }
}
