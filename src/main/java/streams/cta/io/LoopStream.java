/**
 * 
 */
package streams.cta.io;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.Stream;
import stream.io.multi.AbstractMultiStream;

/**
 * @author chris
 *
 */
public class LoopStream extends AbstractMultiStream {

	static Logger log = LoggerFactory.getLogger(LoopStream.class);

	int idx = 0;
	int events = 100;
	ArrayList<Data> items = new ArrayList<Data>();

	/**
	 * @see stream.io.multi.AbstractMultiStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();

		Map<String, Stream> streams = this.getStreams();
		log.info("Found {} inner streams", streams.size());

		for (Stream stream : streams.values()) {
			stream.init();
			Data item = stream.read();
			while (item != null && items.size() < events) {
				items.add(item);
				item = stream.read();
			}

			if (items.size() >= events) {
				break;
			}
		}

		log.info("Loaded {} events to inner queue.", items.size());
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {
		Data item = items.get(idx % items.size());
		idx = (idx + 1) % items.size();
		return item;
	}

	/**
	 * @return the events
	 */
	public int getEvents() {
		return events;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(int events) {
		this.events = events;
	}
}