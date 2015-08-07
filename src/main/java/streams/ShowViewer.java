/**
 *
 */
package streams;

import streams.cta.CTATelescope;
import streams.hexmap.ui.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chris
 *
 */
public class ShowViewer implements StatefulProcessor {

	static Logger log = LoggerFactory.getLogger(ShowViewer.class);
	Viewer viewer = null;
	AtomicBoolean lock = new AtomicBoolean(true);


    @Parameter(required = false, description = "The default plot range in the main viewer")
    private Integer[] range;
    public void setRange(Integer[] range) {
        if(range.length != 2){
            throw new RuntimeException("The plotrange has to consist of two numbers");
        }
        this.range = range;
    }




    @Override
    public void init(ProcessContext context) throws Exception {
        String os = System.getProperty("os.name");
        log.info("Opening viewer on OS: " + os);
    }


	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(final Data input) {

        lock.set(true);

		Thread t = new Thread() {
			public void run() {
				if (viewer == null) {
					viewer = Viewer.getInstance();
                    if (range != null){
                        viewer.setRange(range);
                    }
					viewer.getNextButton().setEnabled(true);
					viewer.getNextButton().addActionListener(
							arg0 -> {
                                synchronized (lock) {
                                    lock.set(!lock.get());
                                    log.debug("Notifying all listeners on lock...");
                                    lock.notifyAll();
                                }
                            });
				}
				viewer.setVisible(true);
//                EventData event = (EventData) input.get("@event");
                LocalDateTime timeStamp = (LocalDateTime) input.get("@timestamp");
				CTATelescope telescope = (CTATelescope) input.get("@telescope");
				short[][] data = (short[][]) input.get("@rawdata");
				viewer.setDataItem(input, timeStamp, telescope, data);
			}
		};
		t.start();

		synchronized (lock) {
			while (lock.get()) {
				try {
					log.debug("Waiting on lock...");
					lock.wait();
					log.debug("Notification occured on lock!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return input;
	}



    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}