#!/usr/bin/env bash

if [ "$1" == "deployRun" ] || [ $# -eq 0 ]; then
    # package for deployment
    mvn -P deploy package

    # package for local start
    mvn -P standalone package
fi

if [ "$1" == "run" ] || [ "$1" == "deployRun" ]; then
    # start the deployment
    java -jar -Dnimbus.host=localhost -Dstorm.jar=target/cta-tools-0.0.1-SNAPSHOT-storm-provided.jar target/cta-tools-0.0.1-SNAPSHOT-storm-compiled.jar src/test/resources/performance-synthetic-stream.xml
fi


#java -jar -Dnimbus.host=129.217.160.98 -Dstorm.jar=target/cta-tools-0.0.1-SNAPSHOT-storm-provided.jar target/cta-tools-0.0.1-SNAPSHOT-storm-compiled.jar streams-processes/storm/subscribe.xml
