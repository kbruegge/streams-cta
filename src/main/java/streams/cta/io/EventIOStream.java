package streams.cta.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import streams.cta.CTAEvent;

/**
 * Created by alexey on 02.06.15.
 */
public class EventIOStream extends AbstractStream {

    static Logger log = LoggerFactory.getLogger(EventIOStream.class);

    static boolean reverse = false;

    private DataInputStream dataStream;

    public static HashMap<Integer, String> eventioTypes;

    public EventIOData eventData;
    public EventIOBuffer buffer;

    @Parameter(
            required = false,
            description = "This value defines the size of the buffer of the BufferedInputStream",
            defaultValue = "8*1024")

    private int bufferSize = 8 * 1024;

    public EventIOStream(SourceURL url) {
        super(url);
    }

    public EventIOStream() {
        super();
    }

    @Override
    public void init() throws Exception {
        super.init();

        eventData = new EventIOData();

        // try opening file from the given URL
        File f = new File(this.url.getFile());
        if (this.url.getProtocol().toLowerCase().startsWith("file")
                && !f.canRead()) {
            log.error("Cannot read file. Wrong path? " + f.getAbsolutePath());
            throw new FileNotFoundException("Cannot read file "
                    + f.getAbsolutePath());
        }

        BufferedInputStream bStream = new BufferedInputStream(
                url.openStream(),
                bufferSize);
        dataStream = new DataInputStream(bStream);

        buffer = new EventIOBuffer(dataStream);

        // import the registered types
        importEventioRegisteredDatatypes();
    }

    @Override
    public Data readNext() throws Exception {

        Data item = null;
        EventIOHeader header = new EventIOHeader(buffer);
        if (header.findAndReadNextHeader()) {

            // MC Shower
            if (header.type == 2020) {
                eventData.mcShower = readMCShower(dataStream, header);
                item = DataFactory.create();
                CTAEvent event = new CTAEvent(10, new byte[]{0, 1, 2});
                item.put("@event", event);
            } else {
                byte[] bytes = new byte[header.length];
                dataStream.read(bytes);
                CTAEvent event = new CTAEvent(0, bytes);
                item = DataFactory.create();
                item.put("@event", event);
            }
            header.getItemEnd();
        }
        reverse = false;
        return item;
    }

    private MCShower readMCShower(DataInputStream dataStream, EventIOHeader header)
            throws IOException {
        if (header.version > 2) {
            log.error("Unsupported MC shower version: " + header.version);
            dataStream.skipBytes(header.length);
            return null;
        }

        MCShower mcShower = new MCShower();
        mcShower.shower_num = header.identification;

        mcShower.primary_id = buffer.readInt32(); // int32
        mcShower.energy = buffer.readReal(); // real
        mcShower.azimuth = buffer.readReal(); // real
        mcShower.altitude = buffer.readReal(); // real
        if (header.version >= 1) {
            mcShower.depth_start = buffer.readReal(); // real
        }
        mcShower.h_first_int = buffer.readReal();
        mcShower.xmax = buffer.readReal();
        mcShower.hmax = mcShower.emax = mcShower.cmax = 0d;

        if (header.version >= 1) {
            mcShower.hmax = buffer.readReal();
            mcShower.emax = buffer.readReal();
            mcShower.cmax = buffer.readReal();
        }

        mcShower.num_profiles = buffer.readInt16(); // short

        //TODO: this should be in a file containing all constants
        int H_MAX_PROFILE = 10;

        for (int i = 0; i < mcShower.num_profiles && i < H_MAX_PROFILE; i++) {
            int skip = 0;
            ShowerProfile profile = new ShowerProfile();
            profile.id = buffer.readInt32();
            profile.num_steps = buffer.readInt32();
            if (profile.num_steps > profile.max_steps) {
                if (profile.content != null) {
                    if (profile.max_steps > 0) {
                        profile.content = null;
                    } else {
                        skip = 1;
                    }
                }
            }

            profile.start = buffer.readReal();
            profile.end = buffer.readReal();

            if (profile.num_steps > 0) {
                profile.binsize = (profile.end - profile.start) / (double) profile.num_steps;
            }
            if (profile.content == null) {
                profile.content = new double[profile.num_steps];

                // here in original code there is a check
                // whether content could have been allocated
                // otherwise there were too little space

                profile.max_steps = profile.num_steps;
            }

            if (skip == 1) {
                for (int j = 0; j < profile.num_steps; j++) {
                    buffer.readReal();
                }
                profile.num_steps *= -1;
            } else {
                profile.content = buffer.readVectorOfReals(profile.num_steps, dataStream);
            }
            mcShower.profile[i] = profile;
        }

        if (header.version >= 2) {
            mcShower.extra_parameters = readShowerExtraParameters(dataStream, header);
        } else {
            clearShowerExtraParameters(mcShower.extra_parameters);
        }
        return mcShower;
    }

    private void clearShowerExtraParameters(ShowerExtraParameters extra_parameters) {
        //TODO: implement
        extra_parameters.id = 0;
        extra_parameters.is_set = 0;
        extra_parameters.weight = 1.0;

//        if ( ep->iparam != NULL )
//        {
//            for (i=0; i<ep->niparam; i++)
//                ep->iparam[i] = 0;
//        }
//        if ( ep->fparam != NULL )
//        {
//            for (i=0; i<ep->nfparam; i++)
//                ep->fparam[i] = 0;
//        }
    }

    private ShowerExtraParameters readShowerExtraParameters(DataInputStream dataStream, EventIOHeader header) throws IOException {
        //TODO: implement
        ShowerExtraParameters ep = new ShowerExtraParameters();
        ep.is_set = 0;

        //TODO: get begin of an item

        //TODO: go to the end if version != 1

        ep.id = header.identification;
        ep.weight = buffer.readReal();


        return null;
    }

    /**
     * Read the file 'EventioRegisteredNames.dat' and import the types mentioned there.
     */
    private void importEventioRegisteredDatatypes() {
        InputStream fileInput = EventIOStream.class.getResourceAsStream("/EventioRegisteredNames.dat");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput));
        String line;

        eventioTypes = new HashMap<>();
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] lineSplit = line.split(":");
                eventioTypes.put(Integer.valueOf(lineSplit[0]), lineSplit[1]);
            }
        } catch (IOException e) {
            log.error("Error while trying to read the EventioRegisteredNames.dat in order to import the datatypes.");
        }
    }

    private void setReverse(boolean reverse) {
        EventIOStream.reverse = reverse;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    class EventIOData {
        MCShower mcShower;
    }
}
