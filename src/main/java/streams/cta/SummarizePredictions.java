package streams.cta;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import stream.Data;
import stream.Keys;
import stream.Processor;

/**
 * Add predictions telescope-wise predictions for energy and signal/background to the data stream.
 * Created by kbruegge on 3/1/17.
 */
public class SummarizePredictions implements Processor {

    @Override
    public Data process(Data item) {
        SummaryStatistics energy = new SummaryStatistics();
        for (String k : Keys.select(item, "*:prediction:energy")){
                energy.addValue((Double) item.get(k));
        }

        SummaryStatistics signal = new SummaryStatistics();
        for (String k : Keys.select(item, "*:prediction:signal")){
            signal.addValue((Double) item.get(k));
        }

        
        item.put("prediction:signal:mean", signal.getMean());
        item.put("prediction:signal:sum", signal.getSum());
        item.put("prediction:signal:max", signal.getMax());
        item.put("prediction:signal:min", signal.getMin());
        item.put("prediction:signal:std", signal.getStandardDeviation());

        item.put("prediction:energy:mean",energy.getMean());
        item.put("prediction:energy:max", energy.getMax());
        item.put("prediction:energy:min", energy.getMin());
        item.put("prediction:energy:std", energy.getStandardDeviation());
        return item;
    }
}
