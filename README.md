# streams-cta

The realtime analysis of the data from the CTA arrays poses interesting challenges to the design of a proper execution environment, that is capable of performing the required steps of data calibration, image cleaning and alert detection.

With the **streams-cta** project we investigate the use of highly scalable platforms, such as [Apache Storm](http://storm.apache.org/) and [Apache Kafka](http://kafka.apache.org/), that recently have been published by the computer science community to tackle Big Data analysis requirements. Based on the intermediate streams framework, which serves as a middle-layer design tool, we test different implementations and platforms for their performance and scalability.


## Data Model

To use the ProtoBuf data models from DAQ we use the alpha version of the javanano output of protocolbuffers version 3.
To get the javanano output download the current protobuf implementation from [github](https://github.com/google/protobuf/tree/master/javanano).
Compile and install everything needed. Then you can call it with with following options:


        protoc --javanano_out=../streams-cta/src/main/java/
            -I../streams-cta/src/main/java/streams/cta/io/datamodel/
            ../streams-cta/src/main/java/streams/cta/io/datamodel/L0.proto
            ../streams-cta/src/main/java/streams/cta/io/datamodel/CoreMessages.proto


Be aware that the package names in the .proto files have been changed from `DataModel` to `package streams.cta.io.datamodel;`




### Usage

You can read in .kryo files or eventio if you dare and then simply use the  `CameraServerPublisher`to push them to zeromq.

        <streams.cta.io.CameraServerPublisher addresses="tcp://*:${port}" />

To read data either from Etienne DummyCameraServer or from another stream use

        <stream id="cta:data" class="streams.cta.io.CameraServerStream" addresses="tcp://127.0.0.1:4849"/>


## EventIO files

Three different eventio files can be downloaded from SFB876 homepage and used for testing purposes (please, contact us, if you are interested in more details and do not have login for SFB876):

* [small (~780 mb)](http://sfb876.tu-dortmund.de/auto?self=$eg7ezym8sg)
* [medium (~1.9 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7fcd8vsw)
* [large (~3.1 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7gpm8000)

## Code Style

For this project we intend to use [Java Code Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html) suggested by google.

In case you're using Java IDE such as IntelliJ or Eclipse you can simply import this style guide by following the simple [instruction](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse).
For Mac users the path to the codestyles folder is: ```~/Library/Preferences/IdeaICxx/codestyles```
Afterwards your IDE can e.g. reformat your code to the Code Style suggested there (in IntelliJ: ```Code```-> ```Reformat Code...```).


# Streams using Storm
As CTA is going to produce a huge stream of data, one need to ensure to have enough machines to process this data.
Apache Storm is an approach for simple handling of a cluster and deploying tasks (a.k.a topologies) for processing the data.

## Packaging on its own

In order to build and deploy cta-streams package to a local storm installation we need two ``jar`` packages: 

* one to **transform** the given streams XML into a topology which will be run in ``storm``
* another one package will be **deployed to nimbus** node of storm cluster and used later by the workers to run the topology

Thus, we need two lines of code to produce those packages:

```bash
# package for deployment
mvn -P deploy package

# package for local start and transformation step
mvn -P standalone package
```

The result of such a bash script are two files:

```bash
# does not contain storm, will be deployed
cta-tools-0.0.1-SNAPSHOT-storm-provided.jar 

# contains storm, run locally
cta-tools-0.0.1-SNAPSHOT-storm-compiled.jar 
```
Afterwards all you need to do is 

```
java -jar -Dnimbus.host=localhost -Dstorm.jar=target/cta-tools-0.0.1-SNAPSHOT-storm-provided.jar target/cta-tools-0.0.1-SNAPSHOT-storm-compiled.jar streams_process.xml
```

``-Dnimbus.host`` is used to define the IP adress of the storm nimbus and ``-Dstorm.jar`` declares the path to the jar package that will be deployed to the storm cluster.
Jar file containing storm is used to run Storm Topology Builder on your machine and run the transformation and deployment process.

## Using script

As a shortcut there is a script ``packageScriptForDeploy.sh`` in the top level of this repository. It accepts as argument 

* ``deployRun``: package everything and deploy to local storm installation with a provided xml file
* ``run``: don't package (use existing packages) and just deploy those packages with a provided xml file
* With no parameter given this script just packages everything and doesn't deploy anything.

# Start storm locally

To run storm you need Java 6 and Python 2.6.6. 

* Download [storm](https://storm.apache.org/downloads.html)
* Download [zookeeper](http://zookeeper.apache.org/releases.html)
* Start zookeeper (``bin/zkServer.sh start``), [more details](http://zookeeper.apache.org/doc/r3.3.3/zookeeperStarted.html#sc_InstallingSingleMode)
* From the unzipped storm archive run: 
  * ``bin/storm nimbus`` to start nimbus (master node)
  * ``bin/storm ui`` to start the graphical interface (``localhost:8080``)
  * ``bin/storm supervisor`` to start a supervisor with 4 worker nodes (threads)

For more tuning possibilities you will find ``conf/storm.yaml`` with properties.

**IMPORTANT**: The above setting only works for one supervisor on the local machine. 
You can not start two supervisors this way.
The easieast solution is to copy the whole storm folder and start a supervisor with differnt ports by changing those properties in ``conf/storm.yaml``.

**MORE DETAILS** can be found on the [documentation pages of Apache Storm](https://storm.apache.org/documentation/Setting-up-a-Storm-cluster.html).
