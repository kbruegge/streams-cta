/**
 * 
 */
package streams.cta.container;

import streams.cta.CTATelescope;
import streams.cta.CTATelescopeType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class contains the extracted data for each pixel in a single telescope.
 *
 * @author Kai
 */
public class ExtractedData implements Serializable {
    public final double[][] arrivalTime;
    public final double[][] photons;

    public ExtractedData(double[][] arrivalTime, double[][] photons) {
        this.arrivalTime = arrivalTime;
        this.photons = photons;
    }
}
