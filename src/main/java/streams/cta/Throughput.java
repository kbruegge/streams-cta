/**
 * 
 */
package streams.cta;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.data.DataFactory;

/**
 * @author chris
 * 
 */
public class Throughput extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(Throughput.class);

	final static List<Data> measurements = new ArrayList<Data>();

	String id = "?";
	Long count = 0L;
	Long first = null;
	Long last = System.currentTimeMillis();

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if (first == null) {
			first = System.currentTimeMillis();
		}

		count++;
		last = System.currentTimeMillis();
		return input;
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();

		if (System.getProperty("type") != null) {
			id = System.getProperty("type");
		}

		Long time = last - first;
		Double rate = (count.doubleValue() / (1 + (time.doubleValue() / 1000.0)));

		Data measure = DataFactory.create();
		measure.put("id", id);
		measure.put("start", first);
		measure.put("end", last);
		measure.put("count", count);
		measure.put("rate", rate);

		measurements.add(measure);

		log.info("Measured throughput of {} elements.", count);
		log.info("Total time was {} ms.", time);

		log.info("Data rate was {} evts/second", rate);
	}

}
