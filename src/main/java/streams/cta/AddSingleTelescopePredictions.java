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
public class AddSingleTelescopePredictions implements Processor {

    @Service
    private PredictionService regressor;

    @Service
    private PredictionService classifier;

    @Override
    public Data process(Data item) {
        int[] triggeredTelescopes = (int[]) item.get("array:triggered_telescopes");

        SummaryStatistics energyStats = new SummaryStatistics();
        SummaryStatistics predictionStats = new SummaryStatistics();

        for(int id : triggeredTelescopes) {

            Set<String> selectedKeys = Keys.select(item, "telescope:" + id + ":*");
            for (String key : selectedKeys) {
                List<String> splitToList = Splitter.on(":")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(key);

                //remove first two elements (telescope:*:)
                List<String> subList = splitToList.subList(2, splitToList.size());

                String newKey = Joiner.on(":").join(subList);
                item.put(newKey, item.get(key));
            }

            double energyPrediction = regressor.applyRegression(item);
            //signal class has name '1' I believe.
            ProbabilityDistribution classification = classifier.applyClassifier(item);

            item.put("telescope:" + id + ":prediction:energy", energyPrediction);
            item.put("telescope:" + id + ":prediction:signal", classification.getProbability("1"));
            item.put("telescope:" + id + ":prediction:background", classification.getProbability("0"));

            energyStats.addValue(energyPrediction);
            predictionStats.addValue(classification.getProbability("1"));
        }
        Keys.select(item, "shower*").forEach(item::remove);

        item.put("prediction:signal:mean", predictionStats.getMean());
        item.put("prediction:signal:max", predictionStats.getMax());
        item.put("prediction:signal:min", predictionStats.getMin());
        item.put("prediction:signal:std", predictionStats.getStandardDeviation());

        item.put("prediction:energy:mean",energyStats.getMean());
        item.put("prediction:energy:max", energyStats.getMax());
        item.put("prediction:energy:min", energyStats.getMin());
        item.put("prediction:energy:std", energyStats.getStandardDeviation());
        
        return item;
    }
}
