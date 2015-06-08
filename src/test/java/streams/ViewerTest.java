package streams;

import org.junit.Test;
import stream.Data;
import streams.cta.TelescopeEvent;
import streams.cta.io.SyntheticEventStream;
import streams.hexmap.ui.Viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 20.03.14.
 */
public class ViewerTest {

    SyntheticEventStream syntheticEventStream = new SyntheticEventStream();
    AtomicBoolean lock = new AtomicBoolean(true);
    Viewer viewer = Viewer.getInstance();

    //this can't be called during an automated unit test cause it needs some user input to exit
    @Test
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
                            new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent arg0) {
                                    synchronized (lock) {
                                        lock.set(!lock.get());
                                        lock.notifyAll();
                                    }
                                }
                            });
                }
                TelescopeEvent telescopeEvent = (TelescopeEvent) item.get("@event");
                viewer.setDataItem(item, telescopeEvent);
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

    @Test
    public void testViewerXML() throws Exception {
        final URL url = ViewerTest.class
                .getResource("/viewer.xml");
        stream.run.main(url);
    }
}
