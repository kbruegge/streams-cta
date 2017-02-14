package streams.hexmap;

/**
 * This describes the stuff a pixel to be drawn on the  must have. This should be subclassed if you need
 * more information in each pixel
 * Created by kaibrugge on 23.04.14.
 */
public class Signal {


    class Pixel {
        final public int id;
        final public double weight;
        final public double xPositionInMM;
        final public double yPositionInMM;


        public Pixel(int id, double xPositionInM, double yPositionInM, double weight) {
            this.id = id;
            this.xPositionInMM = xPositionInM;
            this.yPositionInMM = yPositionInM;
            this.weight = weight;
        }
    }




//
//
//    @Override
//    public String toString(){
//        return "Pixel " + this.id + " at position " + xPositionInMM + ", " + yPositionInMM;
//    }


}
