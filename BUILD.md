# Building the streams-cta Pipeline

The whole pipeline is Maven-based Java project, which is available as source
code from the following Bitbucket repository:

	    https://bitbucket.org/cbockermann/streams-cta/


To build the project, you simple need to clone the repository locally and
compile and package it using the Maven build-tool:

        # git clone https://cbockermann@bitbucket.org/cbockermann/streams-cta.git
        # mvn package

This will produce an executable Jar file in the `target/` folder. The Jar file
is called `cta-tools-VERSION.jar`, where `VERSION` is the latest version of the
project.



# Running the streams-cta Pipeline

For running the pipeline, a XML configuration of the pipeline is required. Then,
the pipeline can be started by running

     # java -jar cta-tools-VERSION.jar /path/to/pipeline-config.xml

In the following, we will outline a simple pipeline configuration as a starting point.



# Connecting to the Camera-Server

The streams-cta library provides a *CameraServerStream* class, which allows for
connecting to a Zero-MQ socket and reading the data provided by the *CameraServer*
program as provided by Etienne.

The following XML definition defines a simple pipeline, that connects to a
CameraServer which publishes its messages on port `5000`:

       <application>
           <stream id="cta:events" class="streams.cta.io.CameraServerStream"
                  addresses="tcp://camera-server-ip:5000/" />

           <process input="cta:events">
                <streams.DataRate every="10000" logmemory="true"/>
           </process> 
       </application>


The pipeline process defines a single function, which outputs the current data
rate every 10.000 events.


For more complex pipelines, the Bitbucket project contains several examples
in the [streams-processes](https://bitbucket.org/cbockermann/streams-cta/src/58e53a9f0fbcb9c3199f9235ae9019c9ae981945/streams-processes/?at=master) folders, e.g. in `streams-processes/daq/`.

These pipelines do provide additional functions in the `process` definition
that apply basic operations and compute some features from the received data.