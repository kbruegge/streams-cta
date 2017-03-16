package streams.cta.io;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Keys;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CSVWriter writes out the values for the given keys to a csv file.
 * The first line of the CSV file is a header.
 * The header contains the names of the keys found in the data item.
 *
 * @author kai
 */
public class CSVWriter implements StatefulProcessor {

    Logger log = LoggerFactory.getLogger(CSVWriter.class);

    @Parameter(required = true, description = "The url to write to")
    SourceURL url;

    @Parameter(required = true)
    Keys keys;


    private boolean headerWritten = false;
    private PrintWriter writer;
    private String seperator = ",";


    @Override
    public void init(ProcessContext processContext) throws Exception {
        String filePath = url.getFile();
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs() && file.createNewFile()) {
                    log.info("Created path to " + file.toString());
                } else {
                    log.error(file.toString() + " could not have been created.");
                }
            }
        }
        writer = new PrintWriter(new FileOutputStream(file));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        writer.flush();
        writer.close();
    }

    @Override
    public Data process(Data data) {
        Set<String> selectedKeys = keys.select(data);
        if (!headerWritten){
            String header = Joiner.on(seperator).join(selectedKeys);
            writer.println(header);
            headerWritten = true;
        }

        List<String> values = selectedKeys.stream()
                                          .map(data::get)
                                          .map(String::valueOf)
                                          .collect(Collectors.toList());

        String joinedValues = Joiner.on(seperator).join(values);
        writer.println(joinedValues);
        writer.flush();
        return data;
    }
}
