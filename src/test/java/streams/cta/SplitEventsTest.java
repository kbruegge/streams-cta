package streams.cta;

import org.junit.Test;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.io.CsvStream;
import stream.io.SourceURL;
import streams.cta.io.ImageStream;

import java.net.URL;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the splitting of data items by telescope id.
 *
 * Created by mackaiver on 13/07/17.
 */
public class SplitEventsTest {

    @Test
    public void testSplittingTelescopes() throws Exception {
        URL url = SplitByTelescope.class.getResource("/images.json.gz");

        ImageStream stream = new ImageStream(new SourceURL(url));
        stream.init();

        Data data = stream.read();

        Processor splitter = new SplitByTelescope();
        data = splitter.process(data);

        int numTriggeredTelescopes = (int) data.get("array:num_triggered_telescopes");
        Data[] telItems = (Data[]) data.get("@telescopes");

        assertThat(telItems.length, is(numTriggeredTelescopes));

        String sourceFile = (String) data.get("source_file");
        for (Data d : telItems){
            assertThat(d.get("source_file"), is(sourceFile));
        }

    }


}
