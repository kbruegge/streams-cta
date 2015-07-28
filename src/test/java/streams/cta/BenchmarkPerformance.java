package streams.cta;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import stream.Data;
import stream.io.SourceURL;
import streams.cta.io.EventIOStream;

/**
 * Created by kai on 02.06.15.
 */
@State(Scope.Benchmark)
public class BenchmarkPerformance {

    private EventIOStream stream;
    private Throughput throughput;

    @Setup
    public void setupBenchmark() throws Exception {
        stream = new EventIOStream(
                new SourceURL("file:../gamma_20deg_180deg_run61251___cta-prod2-sc-sst-x_desert-1640m-Aar.simtel"));
        stream.init();
        throughput = new Throughput();
        throughput.init(null);
    }

    @Benchmark
    public void benchmarkSyntheticStream() throws Exception {
        Data item = stream.readNext();
        throughput.process(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkPerformance.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(8)
                .forks(4)
                .build();

        new Runner(opt).run();
    }


}
