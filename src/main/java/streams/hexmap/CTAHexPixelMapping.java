/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.net.URL;

/**
 * This class provides a mapping between different Pixel ids and geometric information from a (so far) generic CTA LST layout
 *
 * @author Kai
 * 
 */
public class CTAHexPixelMapping extends HexPixelMapping {


    static Logger log = LoggerFactory.getLogger(CTAHexPixelMapping.class);



    public final int numberOfPixel = 1855;


    private static CTAHexPixelMapping mapping;

    public static CTAHexPixelMapping getInstance() {
        if (mapping ==  null){
            String pixelMap = "/hexmap/pixel-map-cta.csv";
            URL mapUrl = CTAHexPixelMapping.class.getResource(pixelMap);
            if(mapUrl == null){
                String msg = "Could not load pixel mapping from URL: " + pixelMap + ". Does the file exist?";
                log.error(msg);
                throw new InstantiationError(msg);
            } else {
                mapping = new CTAHexPixelMapping(mapUrl);
            }
        }
        return mapping;
    }

    private CTAHexPixelMapping(URL mappingURL) {
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
    @Override
    protected CameraPixel getPixelFromCSVItem(Data item){
        int id = (int)(item.get("id"));
        int geometricX = (Integer)(item.get("geom_x"));
        int geometricY = (Integer)(item.get("geom_y"));
        double posX = Double.parseDouble(item.get("position_x").toString());
        double posY = Double.parseDouble(item.get("position_y").toString());

        return new CameraPixel(id, geometricX, geometricY, posX, posY);
    }





    @Override
    public int getNumberOfPixel() {
        return numberOfPixel;
    }


}