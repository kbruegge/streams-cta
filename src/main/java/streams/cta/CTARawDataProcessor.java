package streams.cta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import streams.hexmap.Shower;

/**
 * Abstract processor class that extracts event data, telescope and timestamp information from a
 * telescope event and calls process method with those extracted values. New processors can
 * implement new processors based on this one using a process method with already extracted values.
 *
 * @author kai
 */
public abstract class CTARawDataProcessor implements Processor {

    @Override
    public Data process(Data input) {

        int[] triggeredTelescopes = (int[]) input.get("triggered_telescopes:ids");
        HashMap<Integer, double[]> map = new HashMap<>();
        for (int i : triggeredTelescopes){
            double[] image = (double[]) input.get("telescope:" + i + ":raw:photons");
            map.put(i, image);
        }


        return process(input, map);
    }

    public abstract Data process(Data input, Map<Integer, double[]> images);
}
