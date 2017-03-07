package streams.cta;

import org.jpmml.evaluator.ProbabilityDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;
import stream.io.SourceURL;

/**
 * Add predictions telescope-wise predictions for energy and signal/background to the data stream.
 * Created by kbruegge on 3/1/17.
 */
public class TelescopePredictions implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(TelescopePredictions.class);


    @Service
    private PredictionService regressor;

    @Service
    private PredictionService classifier;

    @Parameter(required=false)
    private boolean local;

    @Override
    public Data process(Data item) {

        double energyPrediction = regressor.applyRegression(item);
        //signal class has name '1' I believe.
        ProbabilityDistribution classification = classifier.applyClassifier(item);

        item.put("prediction:energy", energyPrediction);
        item.put("prediction:signal", classification.getProbability("1"));
        item.put("prediction:background", classification.getProbability("0"));

        return item;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        if (local){
            log.debug("Creating local instances of prediction services.");

            SourceURL url = classifier.url;
            classifier = new PredictionService();
            classifier.url = url;
            classifier.init();

            url = regressor.url;
            regressor = new PredictionService();
            regressor.url = url;
            regressor.init();
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
