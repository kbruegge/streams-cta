package streams;

import com.google.common.base.Stopwatch;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.concurrent.TimeUnit;

/**
 * Created by kai on 11.08.15.
 */
public class DataRate implements StatefulProcessor {
    Logger log = LoggerFactory.getLogger(DataRate.class);

    long totalItems = 0;
    long itemCounter = 0;


    @Parameter(required = false, description = "How many data items are collected for each measurement of the data rate")
    long every = 200;


    @Parameter(required = false, description = "Flags whether to print stuff to console during processing or not.")
    boolean silent = false;


    SummaryStatistics statistics = new SummaryStatistics();
    private Stopwatch stopwatch;

    @Override
    public void init(ProcessContext context) throws Exception {
        stopwatch = Stopwatch.createUnstarted();
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        log.info("Mean data rate: " + statistics.getMean() + " +- " + statistics.getStandardDeviation()/statistics.getN());
    }

    @Override
    public Data process(Data input) {
        if(totalItems == 0) {
            stopwatch.start();
        }
        if(itemCounter == every){
            double dataRatePerSecond = 1000 * ((double) itemCounter) / (double)stopwatch.elapsed(TimeUnit.MILLISECONDS);
            statistics.addValue(dataRatePerSecond);
            if (!silent) {
                log.info("Current Data rate per second: " + dataRatePerSecond);
            }
            input.put("@datarate", dataRatePerSecond);
            stopwatch.reset();
            stopwatch.start();
            itemCounter = 0;
        }

        itemCounter++;
        totalItems++;
        return input;
    }


    public void setEvery(long every) {
        this.every = every;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }
}
