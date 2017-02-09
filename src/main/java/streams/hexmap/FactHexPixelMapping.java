/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.net.URL;

/**
 * This class provides a mapping between different Pixel ids and geometric information specifically for the FACT camera
 *
 * @author Kai
 * 
 */
public class FactHexPixelMapping extends HexPixelMapping<FactCameraPixel> {


    static Logger log = LoggerFactory.getLogger(FactHexPixelMapping.class);


    private static FactHexPixelMapping mapping;

    public static FactHexPixelMapping getInstance() {
        if (mapping ==  null){
            String pixelMap = "/hexmap/fact-pixel-map.csv";
            URL mapUrl = FactHexPixelMapping.class.getResource(pixelMap);
            if(mapUrl == null){
                String msg = "Could not load pixel mapping from URL: " + pixelMap + ". Does the file exist?";
                log.error(msg);
                throw new InstantiationError(msg);
            } else {
                mapping = new FactHexPixelMapping(mapUrl);
            }
        }
        return mapping;
    }

    private FactHexPixelMapping(URL mappingURL) {
        if(mappingURL.getFile().isEmpty()){
            throw new RuntimeException("Could not find pixel mapping file");
        }
        load(mappingURL);
    }



    /**
     * Takes a data item containing a row from the mapping file.
     *
     * @return a pixel with the info from the item
     */
    protected FactCameraPixel getPixelFromCSVItem(Data item){
        int softID = (int)(item.get("softID"));
        int hardID = (int)(item.get("hardID"));
        int geometricX = (Integer)(item.get("geom_i"));
        int geometricY = (Integer)(item.get("geom_j"));
        //the units in the file are arbitrary
        //convert them to millimeter by multiplying with the pixel diameter
        double posX = Double.parseDouble(item.get("pos_X").toString())*9.5;
        double posY = Double.parseDouble(item.get("pos_Y").toString())*9.5;

        FactCameraPixel p = new FactCameraPixel(softID, hardID, geometricX, geometricY, posX, posY);
        return p;
    }



    @Override
    public int getNumberOfPixel() {
        return 1440;
    }
}