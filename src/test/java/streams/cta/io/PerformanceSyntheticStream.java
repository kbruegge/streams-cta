/**
 * 
 */
package streams.cta.io;

import static org.junit.Assert.fail;

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
	public void test() {
		try {

			final URL url = PerformanceSyntheticStream.class
					.getResource("/performance-synthetic-stream.xml");
			stream.run.main(url);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Error: " + e.getMessage());
		}
	}
}
