package streams.cta.io;

import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;
import stream.runtime.ProcessContextImpl;
import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by Kai on 16.11.15.
 */
public class CameraServerStreamTest {

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    //private volatile boolean isDone = false;
    private volatile boolean hasFailed = false;

    @Test
    public void test() throws Exception{
        Future<?> submitFuture = executor.submit(() -> {
            try {
                testSimplePublish();
            } catch (Exception e) {
                e.printStackTrace();
                hasFailed = true;
            }
        });

        Future<?> receiveFuture = executor.submit(() -> {
            try {
                testReceive();
            } catch (Exception e) {
                e.printStackTrace();
                hasFailed = true;
            }
        });

        submitFuture.get(10, TimeUnit.SECONDS);
        receiveFuture.get(10, TimeUnit.SECONDS);
        assertThat(hasFailed, is(false));
    }


        public void testReceive() throws Exception {
            System.out.println("Starting receiver");
            CameraServerStream stream = new CameraServerStream();
            stream.addresses = new String[]{"tcp://127.0.0.1:4849"};
            stream.init();
            Data item = stream.readNext();
            System.out.println("Item received contains ");
            System.out.println(item.keySet().size());
            assertThat(item, is(notNullValue()));
        }


        public void testSimplePublish() throws Exception {
            System.out.println("Starting publisher");
            CameraServerPublisher publish = new CameraServerPublisher();
            publish.addresses = new String[]{"tcp://127.0.0.1:4849"};
            publish.init(new ProcessContextImpl());

            //create some random data
            int numberOfPixels = 1885;
            int numberOfSlices = 50;
            CTATelescope telescope = new CTATelescope(CTATelescopeType.LST, 1, 0, 0, 0, null, null, null);
            Random random = new Random();
            short[][] data = new short[numberOfPixels][numberOfSlices];
            for (int pixel = 0; pixel < numberOfPixels; pixel++) {
                for (int x = 0; x < numberOfSlices; x++) {
                    //data[pixel][x] = (short) f(x);
                    data[pixel][x] += random.nextGaussian();
                }
            }

            Data item = DataFactory.create();
            publish.process(item, telescope, LocalDateTime.now(), data);
        }



}
