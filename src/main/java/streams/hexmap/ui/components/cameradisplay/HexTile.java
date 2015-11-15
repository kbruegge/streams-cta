package streams.hexmap.ui.components.cameradisplay;

import streams.hexmap.CameraPixel;

import java.awt.*;


/**
 * @author kai
 *
 */
public class HexTile extends Tile {

//	private double height;
//	private double width;
    private double radius;
    private CameraPixel pixel;

    public HexTile(CameraPixel p, double radius, double scale){
        this.pixel = p;
        this.radius = radius*scale;
//        this.width = radius * 2*scale;
//        this.height = radius * Math.sqrt(3);
//        this.height = radius * 2;
//        this.width = radius * Math.sqrt(3);
        this.position = this.getPosition(scale);
        this.polygon = this.getPolygon();
    }

    @Override
    public CameraPixel getCameraPixel() {
        return this.pixel;
    }

    //@Override
	private Point getPosition(double scale){
        if(this.position == null) {
//            int posX = this.pixel.offsetCoordinateX;
//            int posY = this.pixel.offsetCoordinateY;
//
//            //Disclaimer: the precision loss is intentional at this point.
//            int cx =  (posX * (int)(0.75*width));
//            int cy = (posY * (int)(height));
            int cx = (int) (this.pixel.xPositionInMM*scale);
            int cy = (int) (this.pixel.yPositionInMM*scale);
            this.position = new Point( cx, cy );
//            System.out.println("Pixel at position " + position.toString());
        }
        return this.position;
	}

    @Override
    public Polygon getPolygon() {
        double[] alphas = { 0.0 , 1.0471975511965976,  2.0943951023931953,
                            3.141592653589793, 4.1887902047863905,  5.235987755982988};
        if(this.polygon == null) {
            Polygon polygon = new Polygon();
            for (int i = 0; i < 6; i++) {
                double alpha = alphas[i];
                //if you get switch cos and sin in the following statements you will rotate the hexagon by 90 degrees
                //In this case we want the flat edge on the bottom.
                double x = this.radius * Math.sin(alpha);
                double y = this.radius * Math.cos(alpha);

                int px = (int) (this.position.x + x);
                int py = (int) (this.position.y + y);
                polygon.addPoint(px, py);
            }
            this.polygon = polygon;
        }
        return this.polygon;
    }


	public boolean equals( Object o ){
		if( o instanceof HexTile){
			HexTile other = (HexTile) o;
			if( this.pixel.equals(other.getCameraPixel())){
				return true;
			}
		}
		return false;
	}
	
}