# Online Learning

Learning from streams is a hot topic in todays research. The masses of data
often require on-time analysis while only a very limited resources with regard
to CPU and memory are available.

Several algorithms have been proposed for approximating classifiers, counting
elements or clustering items within a data stream.


## The stream-analysis Module

The `stream-analysis` modules of the *streams* library provides implementations
for online methods for analysis such as different approximative counting
algorithms or computation of online statistics (e.g. quantile summaries).

In addition, it incorporates the powerful [MOA](http://moa.cs.waikato.ac.nz)
library, a high-end Java library providing implementations for various online
learning schemes.

### Download and Usage

The `stream-analysis` module is a standalone module available at:
<div style="text-align:center;">
  <a href="http://download.jwall.org/streams/stream-analysis-0.9.5-SNAPSHOT.jar">http://download.jwall.org/streams/stream-analysis-0.9.5-SNAPSHOT.jar</a>
</div>

The above JAR archive contains a ready-to-run package including MOA and the
rest of the *streams* library to start experiments by simply running

       # java -jar stream-analysis-0.9.5-SNAPSHOT.jar your-experiment.xml

Examples for experiments can be found e.g. in [Integrating MOA](moa.html).
