# streams-cta

The realtime analysis of the data from the CTA arrays poses interesting challenges to the design of a proper execution environment, that is capable of performing the required steps of data calibration, image cleaning and alert detection.

With the **streams-cta** project we investigate the use of highly scalable platforms, such as [Apache Storm](http://storm.apache.org/) and [Apache Kafka](http://kafka.apache.org/), that recently have been published by the computer science community to tackle Big Data analysis requirements. Based on the intermediate streams framework, which serves as a middle-layer design tool, we test different implementations and platforms for their performance and scalability.


### Data Structure

The input to the realtime analysis (RTA) are calibrated CTA Array events.
These are the static images (given in estimated number of photons per camera pixel) from each telescope that triggered during the event.
Each telescopes data is stored as a simple array of doubles.  
Each telescope has a unique id. Starting at 1 and counting upwards.
Image features are calculated for each camera separately. 
To introduce hierarchical semantics the data is stored 
according to the following naming scheme proposal: 

1. For per camera/telescope specific image features
       
       ```
       telescope:<id>:<feature-group-name>:<feature-name>:*
       ```
   Some example for the well known hillas parameters
       
       ```
       telescope:<id>:shower:width
       telescope:<id>:shower:cog:x
       telescope:<id>:shower:cog:y
       ```

2. MonteCarlo information that is array wide
       
       ```
        mc:<mc-value-name>
       ```
   So for example the true energy could be stored as 
     ```
     mc:primary_energy
     ```
       
More to come soon. Keep in mind that these are currently only proposals. This might change quickly.

### Input Data

The input to this program are note EventIO files but already calibrated events. These
can be produced from any EventIO file using the `convert_raw_data.py` script in the python
folder. It will create a gzipped json file containing the calibrated images for all the events
in the EventIO file.

### Usage

For now check out the xml in the `streams-processes` folder.

## Code Style
We intend to use [Java Code Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html) suggested by google.

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
