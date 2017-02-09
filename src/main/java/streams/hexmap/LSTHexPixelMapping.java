/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.net.URL;

/**
 * This class provides a mapping between different Pixel ids and geometric information from a LST layout.
 * The LSTs have a pointy_top orientation.
 *
 * @author Kai
 * 
 */
public class LSTHexPixelMapping extends HexPixelMapping {


    static Logger log = LoggerFactory.getLogger(LSTHexPixelMapping.class);



    public final int numberOfPixel = 1855;


    private static LSTHexPixelMapping mapping;

    public static LSTHexPixelMapping getInstance() {
        if (mapping ==  null){
            String pixelMap = "/hexmap/lst-pixel-map.csv";
            URL mapUrl = LSTHexPixelMapping.class.getResource(pixelMap);
            if(mapUrl == null){
                String msg = "Could not load pixel mapping from URL: " + pixelMap + ". Does the file exist?";
                log.error(msg);
                throw new InstantiationError(msg);
            } else {
                mapping = new LSTHexPixelMapping(mapUrl);
            }
        }
        mapping.orientation = Orientation.POINTY_TOP;
        return mapping;
    }

    private LSTHexPixelMapping(URL mappingURL) {
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
        int geometricX = Integer.parseInt(item.get("geom_x").toString());
        int geometricY = Integer.parseInt(item.get("geom_y").toString());
        //convert from meter to millimeter
        double posX = Double.parseDouble(item.get("position_x").toString())*1000;
        double posY = Double.parseDouble(item.get("position_y").toString())*1000 * (-1);

        return new CameraPixel(id, geometricX, geometricY, posX, posY);
    }





    @Override
    public int getNumberOfPixel() {
        return numberOfPixel;
    }


}