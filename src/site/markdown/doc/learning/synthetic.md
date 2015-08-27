Data Generator
==============

Testing online algorithms often requires a large amount of data that
matches a known distribution or can be designed such that specific
test-cases can be created for algorithms.

The *stream-core* package already defines a set of streams for random
data generation. Using the concept of [MultiStreams](../streams.md)
this can easily be used to create tailored data streams.


A Gaussian Stream
-----------------

The `stream.generator.GaussianStream` class implements a data stream
that generates an unlimited sequence of normal distributed data. The
default setup focuses on a single attribute with a mean of 0.0 and
a standard deviation of 1.0:

     <stream id="gauss" class="stream.generator.GaussianStream" />


Using the `attributes` parameter allows to specify the mean and
standard deviation of one or more attributes:

     <stream id="gauss-2" class="stream.generator.GaussianStream"
             attributes="0.0,1.0,2.0,0.25,8.5,2.75" />

The `gauss-2` stream above produces a sequence of data items each of
which holds attributes `x1`, `x2` and `x3` based on the following
distributions:

<table style="margin: auto;">
   <tr>
      <th>Attribute</th>
      <th>Mean</th>
      <th>Standard Deviation</th>
   </tr>
   <tr><td><code>x1</code></td><td>0.0</td><td>1.0</td></tr> 
   <tr><td><code>x2</code></td><td>2.0</td><td>0.25</td></tr>
   <tr><td><code>x3</code></td><td>8.5</td><td>2.75</td></tr>
</table>

The attributes are named `x1`,`x2` and `x3` but can be named according
to a preset using the `keys` attribute:

     <stream id="gauss-2" class="stream.generator.GaussianStream"
             attributes="0.0,1.0,2.0,0.25,8.5,2.75"
             keys="A,B,C" />


Example: A cluster data-stream
==============================

The stream `gauss-2` from above will create a sequence of data items
which are centered around (0.0,2.0,8.5) in a 3-dimensional vector
space.

By combining the concept of
[MultiStreams](../streams.html#MultiStream) with the gaussian streams,
we can easily define a stream that has multiple clusters with
pre-defined centers. The `RandomMultiStream` class is of big use,
here: It allows for randomly picking a substream upon reading each
item. The picks are uniformly distributed over all substreams.

The following definition specifies a stream with data items of 4
clusters with cluster centers (0.0,0.0), (1.0,1.0), (2.0,2.0) and
(3.0,3.0):

    <stream id="clusters" class="stream.io.multi.RandomMultiStream">

        <stream id="cluster-1" class="stream.generator.GaussianStream"
                attributes="1.0,0.0,1.0,0.0" />

        <stream id="cluster-2" class="stream.generator.GaussianStream"
                attributes="2.0,0.0,2.0,0.0" />

        <stream id="cluster-3" class="stream.generator.GaussianStream"
                attributes="3.0,0.0,3.0,0.0" />

        <stream id="cluster-4" class="stream.generator.GaussianStream"
                attributes="4.0,0.0,4.0,0.0" />
    </stream>


### Inbalanced Distributions

In some cases a unified distribution among the sub-streams is not what
is required. The `weights` parameters lets you define a weight for
each substream, resulting in a finer control of the stream. As an
example, the `weights` parameter can be used to create a stream with a
slight fraction of outlier data items:

    <stream id="myStream" class="stream.io.multi.RandomMultiStream"
            weights="0.99,0.01">

        <stream id="normal" class="stream.generator.GaussianStream"
                attributes="1.0,0.0,1.0,0.0" />

        <stream id="outlier" class="stream.generator.GaussianStream"
                attributes="2.0,0.0,2.0,0.0" />
    </stream>

In this example, approximately 1% of the data items is drawn from the
outlier stream, wheras the majority is picked from the "normal"
stream.