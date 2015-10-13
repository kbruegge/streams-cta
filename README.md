# streams-cta

The realtime analysis of the data from the CTA arrays poses interesting challenges to the design of a proper execution environment, that is capable of performing the required steps of data calibration, image cleaning and alert detection.

With the **streams-cta** project we investigate the use of highly scalable platforms, such as [Apache Storm](http://storm.apache.org/) and [Apache Kafka](http://kafka.apache.org/), that recently have been published by the computer science community to tackle Big Data analysis requirements. Based on the intermediate streams framework, which serves as a middle-layer design tool, we test different implementations and platforms for their performance and scalability.

More details can be found in **[Wiki](https://bitbucket.org/cbockermann/streams-cta/wiki/)**.

## EventIO files

Three different eventio files can be downloaded from SFB876 homepage and used for testing purposes:

* [small (~780 mb)](http://sfb876.tu-dortmund.de/auto?self=$eg7ezym8sg)
* [medium (~1.9 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7fcd8vsw)
* [large (~3.1 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7gpm8000)