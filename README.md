# EventIO files #

Three different eventio files can be downloaded from SFB876 homepage.

* [small (~780 mb)](http://sfb876.tu-dortmund.de/auto?self=$eg7ezym8sg)
* [medium (~1.9 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7fcd8vsw)
* [large (~3.1 Gb)](http://sfb876.tu-dortmund.de/auto?self=$eg7gpm8000)

# Streams using Storm
As CTA is going to produce a huge stream of data, one need to ensure to have enough machines to process this data.
Apache Storm is an approach for simple handling of a cluster and deploying tasks (a.k.a topologies) for processing the data.

## Packaging on its own

In order to build and deploy cta-streams package to a local storm installation we need two ``jar`` packages: 

* one to **transform** the given streams XML into a topology which will be run in ``storm``
* another one package will be **deployed to nimbus** node of storm cluster and used later by the workers to run the topology

Thus, we need two lines of code to produce those packages:

```c
# package for deployment
mvn -P deploy package

# package for local start and transformation step
mvn -P standalone package
```

The result of such a bash script are two files:

```c
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


# Code Style #

For this project we intend to use [Java Code Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html) suggested by google.

In case you're using Java IDE such as IntelliJ or Eclipse you can simply import this style guide by following the simple [instruction](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse).
For Mac users the path to the codestyles folder is: ```~/Library/Preferences/IdeaICxx/codestyles```
Afterwards your IDE can e.g. reformat your code to the Code Style suggested there (in IntelliJ: ```Code```-> ```Reformat Code...```).