/**
 * 
 */
package streams.cta.io;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

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
