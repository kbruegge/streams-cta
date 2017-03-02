package streams.cta;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jpmml.evaluator.ProbabilityDistribution;
import stream.*;
import stream.annotations.Service;
import streams.PredictionService;

import java.util.List;
import java.util.Set;

/**
 * Add predictions telescope-wise predictions for energy and signal/background to the data stream.
 * Created by kbruegge on 3/1/17.
 */
public class TelescopePredictions implements Processor {

    @Service
    private PredictionService regressor;

    @Service
    private PredictionService classifier;

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
}
