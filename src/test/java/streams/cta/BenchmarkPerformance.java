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
import streams.cta.io.SyntheticEventStream;

/**
 * Created by kai on 02.06.15.
 */
@State(Scope.Benchmark)
public class BenchmarkPerformance {

    private SyntheticEventStream stream;
    private Throughput throughput;

    @Setup
    public void setupBenchmark() throws Exception {
        stream = new SyntheticEventStream();
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
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(4)
                .build();

        new Runner(opt).run();
    }


}
