/**
 * 
 */
package streams.cta.container;

import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class contains the extracted features for a single telescope and a single event from that telescope.
 * @author Kai
 */
public class FeatureData implements Serializable {
    public final double width, length, delta, alpha, m3long, m3trans;


    public FeatureData(double width, double length,
                       double delta, double alpha, double m3long, double m3trans) {
        this.width = width;
        this.length = length;
        this.delta = delta;
        this.alpha = alpha;
        this.m3long = m3long;
        this.m3trans = m3trans;
    }
}
