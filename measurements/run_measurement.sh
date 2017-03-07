#!/bin/bash
for i in {1..48}
do
   echo "Using $i threads"
   java -jar target/cta-tools-0.0.1-SNAPSHOT.jar total_eventrate.xml -Dnum_copies="$i"
done
