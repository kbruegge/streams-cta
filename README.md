# streams-cta  [![Build Status](https://travis-ci.org/mackaiver/streams-cta.svg?branch=master)](https://travis-ci.org/mackaiver/streams-cta)

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
3. Array wide information. Should contain things like the event timestamp at some point.
For now just trigger information really.

       
       ```
       array:triggered_telescope_ids
       array:num_triggered_telescopes
       
       ```   
   
More to come soon. Keep in mind that these are currently only proposals. This might change quickly.

### Input Data

The input to this program are note EventIO files but already calibrated events. These
can be produced from any EventIO file using the `convert_raw_data.py` script in the python
folder. It will create a gzipped json file containing the calibrated images for all the events
in the EventIO file. Input files can be found on Konrads Bernloehrs Website.
The URLs of the files look like this for the super arrays
    
    https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/electron_20deg_0deg_run732___cta-prod3-merged_desert-2150m-Paranal-subarray-1.simtel.gz
    https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/proton_20deg_0deg_run8050___cta-prod3-merged_desert-2150m-Paranal-subarray-1.simtel.gz
    https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/gamma_20deg_0deg_run8142___cta-prod3-merged_desert-2150m-Paranal-subarray-1.simtel.gz
    
There are 5 sub-arrays to pick from. I don't know what the differences are.
The password is the well known CTA password. Contact me if you don't know it.
These URLs point to some sub arrays I believe

    https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/Remerged-3HB8/proton_20deg_0deg_run7866___cta-prod3-merged_desert-2150m-Paranal-3HB8-NG.simtel.gz
    https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/Remerged-3HB8/gamma_20deg_0deg_run5706___cta-prod3-merged_desert-2150m-Paranal-3HB8-NG.simtel.gz



### Usage

For now check out the xml in the `streams-processes` folder.

## Processing on top of distributed frameworks
As CTA is going to produce a huge stream of data, one need to ensure to have enough machines to process this data.
Apache Storm is an approach for simple handling of a cluster and deploying tasks (a.k.a topologies) for processing the data.

Using maven profiles it is possible to package cta-streams for various distributed processing frameworks.
At the moment following support is enabled:

* Apache Storm using [streams-storm](https://bitbucket.org/cbockermann/streams-storm/)
* Apache Flink using [streams-flink](https://github.com/alexeyegorov/streams-flink)
* Apache Spark Streaming using [streams-spark](https://github.com/alexeyegorov/streams-spark)

For Flink and Spark the packaging is done using one step:

```bash

mvn -P deploy,{flink,spark} package

```

Deploying to a Storm cluster requires two ``jar`` files:

* one to **transform** the given streams XML into a native distributed job definition
* another one package will be **deployed to nimbus** node of storm cluster and used later by the workers to run the topology

Thus, we need two lines of code to produce those packages:

```bash
# package for deployment
mvn -P deploy,storm package

# package for local start and transformation step
mvn -P standalone,storm package
```

As a result following ``jar`` files are produced (one for Flink and Spark, two for Storm):

```bash
# run locally
cta-tools-0.0.1-SNAPSHOT-{platform}-compiled.jar 

# does not contain storm, will be deployed
cta-tools-0.0.1-SNAPSHOT-storm-provided.jar 
```

## Code Style
We intend to use [Java Code Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html) suggested by google.

In case you're using Java IDE such as IntelliJ or Eclipse you can simply import this style guide by following the simple [instruction](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse).
For Mac users the path to the codestyles folder is: ```~/Library/Preferences/IdeaICxx/codestyles```
Afterwards your IDE can e.g. reformat your code to the Code Style suggested there (in IntelliJ: ```Code```-> ```Reformat Code...```).

