# streams-cta

The realtime analysis of the data from the CTA arrays poses interesting challenges to the design of a proper execution environment, that is capable of performing the required steps of data calibration, image cleaning and alert detection.

With the **streams-cta** project we investigate the use of highly scalable platforms, such as [Apache Storm](http://storm.apache.org/) and [Apache Kafka](http://kafka.apache.org/), that recently have been published by the computer science community to tackle Big Data analysis requirements. Based on the intermediate streams framework, which serves as a middle-layer design tool, we test different implementations and platforms for their performance and scalability.

More details can be found in **[Wiki](https://bitbucket.org/cbockermann/streams-cta/wiki/)**.


## Data Model

To use the ProtoBuf data models from DAQ we use the alpha version of the javanano output of protocolbuffers version 3
To get the javanano output download the current protobuf implementation from [github](https://github.com/google/protobuf/tree/master/javanano).
Install and compile al of that stuff. Then call it with with folowing options:


        protoc --javanano_out=../streams-cta/src/main/java/
            -I../streams-cta/src/main/java/streams/cta/io/datamodel/
            ../streams-cta/src/main/java/streams/cta/io/datamodel/L0.proto
            ../streams-cta/src/main/java/streams/cta/io/datamodel/CoreMessages.proto

Be aware that the package names in the .proto files have been changed from `DataModel` to
`package streams.cta.io.datamodel;`



## EventIO files

Three different eventio files can be downloaded from SFB876 homepage and used for testing purposes:

* [small (~780 mb)](http://sfb876.tu-dortmund.de/auto?self=$eg7ezym8sg)
* [medium (~1.9 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7fcd8vsw)
* [large (~3.1 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7gpm8000)

## Code Style

For this project we intend to use [Java Code Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html) suggested by google.

In case you're using Java IDE such as IntelliJ or Eclipse you can simply import this style guide by following the simple [instruction](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse).
For Mac users the path to the codestyles folder is: ```~/Library/Preferences/IdeaICxx/codestyles```
Afterwards your IDE can e.g. reformat your code to the Code Style suggested there (in IntelliJ: ```Code```-> ```Reformat Code...```).