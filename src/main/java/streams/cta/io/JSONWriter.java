package streams.cta.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * Writes a file containing a hopefully valid JSON String on each line. Heres a simple Python script
 * to read it:
 *
 * import json
 *
 * def main(): with open('test.json', 'r') as file: for line in file: event = json.loads(line)
 * print(event['NROI'])
 *
 * if __name__ == "__main__": main()
 *
 *
 * Keep in mind that some events might have keys missing.
 *
 * @author kai bruegge
 */
public class JSONWriter implements StatefulProcessor {


    @Parameter(required = true)
    private String[] keys;

    @Parameter(required = true)
    private URL url;

    @Parameter(required = false)
    private boolean writeBlock;

    private Gson gson;
    private BufferedWriter bw;

    //flags whether the first line has been written
    private boolean firstLine = true;


    /**
     * Initialize Gson and Buffered Writer.
     */
    @Override
    public void init(ProcessContext processContext) throws Exception {
        gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        bw = new BufferedWriter(new FileWriter(new File(url.getFile())));
        if (writeBlock) {
            bw.write("[");
            bw.newLine();
        }
    }

    @Override
    public Data process(Data data) {

        Data item = DataFactory.create();
        item.put("time", LocalDateTime.now().toString());

        String[] evKeys = {"@stream"};
        for (String key : evKeys) {
            if (data.containsKey(key)) {
                item.put(key, data.get(key));
            }
        }

        // add objects by specified keys to data item
        for (String key : keys) {
            item.put(key, data.get(key));
        }

        try {
            if (writeBlock && !firstLine) {
                bw.write(",");
            }
            bw.write(gson.toJson(item));

            bw.newLine();
            bw.flush();
            firstLine = false;
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return data;
    }


    @Override
    public void resetState() throws Exception {
    }

    @Override
    public void finish() throws Exception {
        if (bw != null) {
            if (writeBlock) {
                bw.write("]");
            }
            bw.flush();
            bw.close();
        }
    }


    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setWriteBlock(boolean writeBlock) {
        this.writeBlock = writeBlock;
    }
}
