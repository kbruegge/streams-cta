/**
 *
 */
package streams.cta;

import stream.AbstractProcessor;
import stream.Data;
import streams.cta.container.EventData;

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
        EventData ev = (EventData) input.get("@event");
		eventCount++;
		return input;
	}
}
