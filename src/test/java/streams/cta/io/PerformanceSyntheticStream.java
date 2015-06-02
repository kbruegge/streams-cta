/**
 * 
 */
package streams.cta.io;


import java.net.URL;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class PerformanceSyntheticStream {

	static Logger log = LoggerFactory
			.getLogger(PerformanceSyntheticStream.class);



    @Test
    public void test() throws Exception {
        final URL url = PerformanceSyntheticStream.class
                .getResource("/performance-synthetic-stream.xml");
        stream.run.main(url);
    }
}
