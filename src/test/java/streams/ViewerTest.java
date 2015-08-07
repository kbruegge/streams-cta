package streams;

import stream.Data;
import streams.cta.CTATelescope;
import streams.cta.io.SyntheticEventStream;
import streams.hexmap.ui.Viewer;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by kaibrugge on 20.03.14.
 */
public class ViewerTest {

    SyntheticEventStream syntheticEventStream = new SyntheticEventStream();
    AtomicBoolean lock = new AtomicBoolean(true);
    Viewer viewer = Viewer.getInstance();

    //this can't be called during an automated unit test cause it needs some user input to exit
    public void viewer() throws Exception {

        syntheticEventStream.init();
        lock.set(true);

        Thread t = new Thread() {
            Data item = syntheticEventStream.readNext();
            public void run() {
                if (viewer == null) {
                    viewer = Viewer.getInstance();

                    viewer.getNextButton().setEnabled(true);
                    viewer.getNextButton().addActionListener(
                            arg0 -> {
                                synchronized (lock) {
                                    lock.set(!lock.get());
                                    lock.notifyAll();
                                }
                            });
                }
                viewer.setDataItem(item, (LocalDateTime) item.get("@timeStamp"),(CTATelescope) item.get("@telescope"), (short[][])item.get("@rawdata"));
                viewer.setVisible(true);
            }
        };
        t.start();

        synchronized (lock) {
            while (lock.get()) {
                try {
                    lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void testViewerXML() throws Exception {
        final URL url = ViewerTest.class
                .getResource("/viewer.xml");
        stream.run.main(url);
    }
}
