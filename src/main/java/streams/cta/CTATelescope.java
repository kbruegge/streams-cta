package streams.cta;

import java.io.Serializable;

/**
 * Serializable telescope definition.
 *
 * @author kai
 */
public class CTATelescope implements Serializable {
    public CTATelescopeType type;
    public int telescopeId;
    public double x, y, z;

    public int[] brokenPixel;
    public double[] pixelDelays;
    public double[] pixelGains;

    public CTATelescope(CTATelescopeType type, int telescopeId, double x, double y, double z,
                        int[] brokenPixel, double[] pixelDelays, double[] pixelGains) {
        this.type = type;
        this.telescopeId = telescopeId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.brokenPixel = brokenPixel;
        this.pixelDelays = pixelDelays;
        this.pixelGains = pixelGains;
    }

    private CTATelescope() {

    }
}
