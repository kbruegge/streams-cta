/**
 *
 */
package streams.cta;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 */
public class Throughput extends AbstractProcessor {

//	static Logger log = LoggerFactory.getLogger(Throughput.class);

	long eventCount = 0L;


    /**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		eventCount++;
		return input;
	}
}
