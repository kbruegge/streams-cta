package streams.cta;

/**
 * Description of different Telescope types in the cta array. We assume each camera has only one
 * pixel geometry.
 *
 * @author kai
 */
public enum CTATelescopeType {
    LST(0, Geometry.HEXAGONAL, 1855, 2.0),
    MST_GATE(3, Geometry.HEXAGONAL, 11328, 8),
    SST_CHEC(3, Geometry.HEXAGONAL, 2048, 9.6),
    SST_ASTRI(3, Geometry.RECTANGULAR, 2048, 9.50),;


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
