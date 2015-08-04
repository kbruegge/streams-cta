/**
 * 
 */
package streams.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.ArrayList;

/**
 * This class provides a mapping between different Pixel ids and geometric information from the
 * camera layout.
 *
 * This class can get instantiated as a singleton with the getInstance() method.
 *
 * The geometric coordinates stored in the text file to build this map are stored in the "odd -q" vertical layout
 * See http://www.redblobgames.com/grids/hexagons/ for details and pictures.
 *
 * The coordinates are offset by 22 on the x-axis and by 19 on the y-axis
 *
 * @author Kai
 * 
 */
public class FactHexPixelMapping extends HexPixelMapping {

    //store each pixel by its 'geometric' or axial coordinate.
    private final FactCameraPixel[][] offsetCoordinates = new FactCameraPixel[45][40];
    public final FactCameraPixel[] pixelArray = new FactCameraPixel[1440];
    private final int[] chId2softId = new int[1440];
    private final int[] software2chId = new int[1440];

    private final int[][][] neighbourOffsets = {
            {{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}}, //uneven
            {{1, 1}, {1, 0}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}  //pixel with a even x coordinate
    };

    private int xOffset = 22;
    private int yOffset = 19;


    static Logger log = LoggerFactory.getLogger(FactHexPixelMapping.class);


    private static FactHexPixelMapping mapping;

    public static FactHexPixelMapping getInstance() {
        if (mapping ==  null){
            String pixelMap = "/hexmap/pixel-map.csv";
            URL mapUrl = FactHexPixelMapping.class.getResource(pixelMap);
            if(mapUrl == null){
                String msg = "Could not load pixel mapping from URL: " + pixelMap + ". Does the file exist?";
                log.error(msg);
                throw new InstantiationError(msg);
            }
            mapping = new FactHexPixelMapping(mapUrl);
        }
        return mapping;
    }

    public FactHexPixelMapping(URL url){
        super(url);
    }


    public int getNumberRows() {
        return 45;
    }

    public int getNumberCols() {
        return 40;
    }

//    public FactCameraPixel[] getNeighboursFromID(int id){
//        return getNeighboursForPixel(getPixelFromId(id));
//    }



    /**
     * Takes a data item containing a row from the mapping file.
     *
     * @return a pixel with the info from the item
     */
    private FactCameraPixel getPixelFromCSVItem(Data item){
        FactCameraPixel p = new FactCameraPixel();
        p.setSoftID( (Integer)(item.get("softID"))  );
        p.setHardid( (Integer)(item.get("hardID"))  );
        p.geometricX = (Integer)(item.get("geom_i"));
        p.geometricY = (Integer)(item.get("geom_j"));
        p.posX = Float.parseFloat(item.get("pos_X").toString());
        p.posY = Float.parseFloat(item.get("pos_Y").toString());

        return p;
    }

    /**
     * This expects a file containing information on 1440 Pixel
     * @param mapping url to the mapping file
     */
    @Override
    protected void load(URL mapping){

        //use the csv stream to read stuff from the csv file
        CsvStream stream = null;
        try {
            stream = new CsvStream(new SourceURL(mapping), ",");
            stream.init();
        } catch (Exception e){
            log.error(e.toString());
        }

        //we should sort this by chid
        for (int i = 0; i < 1440; i++) {
            Data item = null;
            try {
                item = stream.readNext();
            } catch (Exception e) {
                log.error(e.toString());
            }
            FactCameraPixel p = getPixelFromCSVItem(item);

            software2chId[p.softid] = p.chid;
            chId2softId[p.chid] = p.softid;

            offsetCoordinates[p.geometricX + xOffset][p.geometricY + yOffset] = p;
            pixelArray[p.id] = p;

        }
	}

    @Override
    public FactCameraPixel getPixelFromOffsetCoordinates(int x, int y){
        if (x + xOffset > 44 || y + yOffset >= 40){
            return null;
        }
        if (x + xOffset < 0  || y + yOffset <0){
            return null;
        }
        return offsetCoordinates[x +xOffset][y + yOffset];
    }


    @Override
    public int getNumberOfPixel() {
        return 1440;
    }

    @Override
    public FactCameraPixel getPixelFromId(int id) {
        return pixelArray[id];
    }

    public int getChidFromSoftID(int softid){
        return software2chId[softid];
    }
    public int getSoftIDFromChid(int chid){
        return chId2softId[chid];
    }

}