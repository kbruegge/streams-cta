package streams.cta;

import java.io.Serializable;

/**
 * Created by Kai on 27.07.15.
 */
public class CTATelescope implements Serializable {
    public final CTATelescopeType type;
    public final int telescopeId;
    public final double x,y,z;

    public final int[] brokenPixel;
    public final double[] pixelDelays;
    public final double[] pixelGains;

    public CTATelescope(CTATelescopeType type, int telescopeId, double x, double y, double z, int[] brokenPixel, double[] pixelDelays,
                        double[] pixelGains) {
        this.type = type;
        this.telescopeId = telescopeId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.brokenPixel = brokenPixel;
        this.pixelDelays = pixelDelays;
        this.pixelGains = pixelGains;
    }
}
