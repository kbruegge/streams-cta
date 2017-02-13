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


For more complex pipelines, the Bitbucket project contains several examples
in the [streams-processes](https://bitbucket.org/cbockermann/streams-cta/src/58e53a9f0fbcb9c3199f9235ae9019c9ae981945/streams-processes/?at=master) folders, e.g. in `streams-processes/daq/`.

These pipelines do provide additional functions in the `process` definition
that apply basic operations and compute some features from the received data.