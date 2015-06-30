package streams.cta.io.MCShower;

/**
 * Created by alexey on 30.06.15.
 */
public class ShowerExtraParameters {
    /**
     * May identify to the user what the parameters should mean.
     */
    long id;

    /**
     * May be reset after writing the parameter block and must thus be set to 1 for each shower for
     * which the extra parameters should get recorded.
     */
    int isSet;

    /**
     * To be used if the weight of a shower may change during processing, e.g. when shower
     * processing can be aborted depending on how quickly the electromagnetic component builds up
     * and the remaining showers may have a larger weight to compensate for that. For backwards
     * compatibility this should be set to 1.0 when no additional weight is needed.
     */
    double weight;

    //TODO: some further parameters need to be implemented
    /**
     * Number of extra integer parameters.
     */
    long niparam;

    /**
     * Space for extra integer parameters, at least of size niparam.
     */
    int[] iparam;

    /**
     * Number of extra floating-point parameters.
     */
    long nfparam;

    /**
     * Space for extra floats, at least of size nfparam.
     */
    double[] fparam;
}
