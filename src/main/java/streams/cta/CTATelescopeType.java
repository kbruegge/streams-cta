package streams.cta;

/**
 * Description of different Telescope types in the cta array. We assume each camera has only one
 * pixel geometry. Created by Kai on 27.07.15.
 */
public enum CTATelescopeType {
    LST(0, Geometry.HEXAGONAL, 1800, 2.0),
    MST(1, Geometry.HEXAGONAL, 1000, 1.80),
    SST(2, Geometry.RECTANGULAR, 400, 2.80),
    SST_ASTRI(3, Geometry.RECTANGULAR, 700, 2.30),;


    final int typeId;
    public final Geometry geometry;
    public final int numberOfPixel;
    public final double fieldOfView;

    CTATelescopeType(int typeId, Geometry geometry, int numberOfPixel, double fieldOfView) {
        this.typeId = typeId;
        this.geometry = geometry;
        this.numberOfPixel = numberOfPixel;
        this.fieldOfView = fieldOfView;
    }

    /**
     * The pixel geometry.
     */
    public enum Geometry {
        RECTANGULAR(0),
        HEXAGONAL(1);

        int geometry;

        Geometry(int geometry) {
            this.geometry = geometry;
        }
    }
}
