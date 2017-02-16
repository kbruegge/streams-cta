package streams.cta;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import stream.Data;
import stream.io.SourceURL;
import streams.cta.cleaning.TailCut;
import streams.cta.features.Moments;
import streams.cta.io.ImageStream;
import streams.cta.io.LoopStream;

import java.net.URL;

/**
 * Bencvhmark a simple process calculating hillas parameter
 * Created by kai on 02.06.15.
 */
@State(Scope.Benchmark)
public class BenchmarkPerformance {

    private LoopStream stream;
    private URL images = ImageStream.class.getResource("/images.json.gz");
    private Moments hillas = new Moments();
    private TailCut tailCut = new TailCut();


    @Setup(Level.Iteration)
    public void setupBenchmark() throws Exception {

        stream = new LoopStream();
        stream.addStream("data", new ImageStream(new SourceURL(images)));
        stream.init();

    }

    @Benchmark
    public Data benchmarkSyntheticStream() throws Exception {
        Data item = stream.readNext();
        tailCut.process(item);
        hillas.process(item);
        return item;
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkPerformance.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(8)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
