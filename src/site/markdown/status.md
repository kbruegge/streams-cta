Overview of the  <code>cta streams</code> Project
======================

So far the cta-streams project has been used for evaluating runtime performance for analyzing CTA raw data.
The simulated data is read from <code>.eventio</code> files produced with the Monte Carlo simulation  [sim_telarray](http://www.mpi-hd.mpg.de/hfm/~bernlohr/sim_telarray/) (provided by K.Bernlöhr).

Based on these simulations, low level data from the LST telescope is read and serialized to a more efficient format named [kryo](https://github.com/EsotericSoftware/kryo).

Methods were implemented to calculate image parameters using naive extraction techniques. The process was parallelised for efficient usage
on multiple CPU cores.

Building on the same technologies as the current DAQ prototype design ([ICRC proceeeding](http://arxiv.org/abs/1508.06473))
data was streamed over network using [ZeroMQ](http://zeromq.org/) and [ProtocolBuffers](https://developers.google.com/protocol-buffers/).

The feature extraction process was successfully deployed on an [Apache Storm](https://storm.apache.org/) cluster.


Next Steps
-------
Robustness and failure tolerance using Apache Storm in combination with [Apache Kafka](http://kafka.apache.org/) will be evaluated.
Better feature extraction methods will be developed once more realistic low level MonteCarlos are available.
