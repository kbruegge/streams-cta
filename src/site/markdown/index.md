
The <code>streams-cta</code> Project
====================================

The realtime analysis of the data from the CTA arrays poses interesting
challenges to the design of a proper execution environment, that is capable
of performing the required steps of *data calibration*, *image cleaning* and
*alert detection*.

With the `streams-cta` project we investigate the use of highly scalable platforms,
such as [Apache Storm](http://storm.apache.org) and [Apache Kafka](http://kafka.apache.org), 
that recently have been published by the computer science community to tackle *Big Data*
analysis requirements. Based on the intermediate `streams` framework, which serves 
as a middle-layer design tool, we test different implementations and platforms for 
their performance and scalability.

<div style="margin:auto; border: none; text-align: center;">
   <img style="margin:auto; height: 200px;" src="doc/architecture2.png" />
   <p>Figure 1: Components of the <code>streams</code> library.</p>
</div>


The `streams-cta` project is a community effort of the TU Dortmund department of
physics and the computer science group of the TU Dortmund, jointly working in the
DFG funded collaborative research center on resource constraint analysis [(SFB-876)](http://sfb876.tu-dortmund.de).