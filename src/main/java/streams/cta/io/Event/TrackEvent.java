package streams.cta.io.Event;

/**
 * Tracking data interpolated for one event and one telescope.
 * Created by alexey on 30.06.15.
 */
public class TrackEvent {
    int telId;            ///< The telescope ID number (1 ... n)
    double azimuthRaw;    ///< Raw azimuth angle [radians from N->E].
    double altitudeRaw;   ///< Raw altitude angle [radians].
    double azimuthCor;    ///< Azimuth corrected for pointing errors.
    double altitudeCor;   ///< Azimuth corrected for pointing errors.
    int rawKnown;         ///< Set if raw angles are known.
    int corKnown;         ///< Set if corrected angles are known.
}
