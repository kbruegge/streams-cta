package streams;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 20.03.14.
 */
public class ViewerXMLTest {

    public static void main(String[] args) throws Exception {
        viewerXML();
    }

    //this cant be called during an automated unitest cause it needs some user input to exit
    public static void viewerXML() throws Exception {

        final URL url = ViewerXMLTest.class
                .getResource("/viewer.xml");
        stream.run.main(url);
    }
}
