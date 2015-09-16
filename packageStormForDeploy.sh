#!/usr/bin/env bash

# default values for variables
process_path="streams-processes/zeromq/subscribe.xml"
run=false
build=false
nimbus="localhost"

# handle the argument options
function USAGE {
    echo "-p    <path to XML process file>"
    echo "-n    <nimbus host ip adress>"
    echo "-r    run the deployment process"
    echo "-b    build new packages"
    exit 1
}

while getopts ":pn: :rb" optname
  do
    case "$optname" in
      "p") process_path=$OPTARG;;
      "r") run=true;;
      "b") build=true;;
      "n") nimbus=$OPTARG;;
      "?") USAGE;;
      ":")
        echo "No argument value for option $OPTARG"
        USAGE
        ;;
      *)
        echo "Unknown error while processing options"
        USAGE
        ;;
    esac
  done

echo ""
echo "Using following process as topology: $process_path"
echo "Rebuild: $build"
echo "Running: $run"
echo "Nimbus host: $nimbus"

# stop and print the usage if both r and b were not set
if [ !$run ] && [ !$build ]; then
    USAGE
fi

# rebuild (package) the needed jars
if $build; then
    # package for deployment
    mvn -P deploy package

    # package for local start
    mvn -P standalone package
fi

# run the deployment process
if $run; then
    # start the deployment
    java -jar \
        -Dnimbus.host=$nimbus \
        -Dstorm.jar=target/cta-tools-0.0.1-SNAPSHOT-storm-provided.jar \
        target/cta-tools-0.0.1-SNAPSHOT-storm-compiled.jar \
        $process_path
fi
