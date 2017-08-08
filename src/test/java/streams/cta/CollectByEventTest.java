package streams.cta;

import org.junit.Test;
import stream.Data;
import stream.Keys;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by mackaiver on 13/07/17.
 */
public class CollectByEventTest {

    @Test
    public void testCollectingById() throws Exception {
        URL url = CollectByEventTest.class.getResource("/single_telescope_events.csv");

        CsvStream stream = new CsvStream(new SourceURL(url));
        CollectByEvent p = new CollectByEvent();

        stream.init();

        Data data = stream.readNext();
        while(data != null){
            p.process(data);
            data = stream.readNext();
        }

    }


    @Test
    public void testCollectSameEvents() throws Exception {
        URL url = CollectByEventTest.class.getResource("/single_telescope_events.csv");

        CsvStream stream = new CsvStream(new SourceURL(url));
        CollectByEvent p = new CollectByEvent();
        stream.init();

        Data data = stream.readNext();
        double previousEnergy = (double) data.get("mc:energy");
        while(data != null){

            double energy = (double) data.get("mc:energy");
            Data newItem = p.process(data);

            if (newItem == null){
                assertThat(energy, is(previousEnergy));
            } else {
                double collectedEnergy = (double) newItem.get("mc:energy");
                assertThat(collectedEnergy , is(previousEnergy));
            }

            previousEnergy = energy;
            data = stream.readNext();
        }

    }


    @Test
    public void testNumberOfEvents() throws Exception {
        URL url = CollectByEventTest.class.getResource("/single_telescope_events.csv");

        CsvStream stream = new CsvStream(new SourceURL(url));
        CollectByEvent p = new CollectByEvent();
        stream.init();

        Data data = stream.readNext();
        while(data != null){

            Data newItem = p.process(data);

            if (newItem != null) {
                int numTelescopes = (int) newItem.get("array:num_triggered_telescopes");
                Set<String> selected = Keys.select(newItem, "telescope:*:id");
                assertThat(selected.size(), is(numTelescopes));
            }
            data = stream.readNext();
        }

    }
}
