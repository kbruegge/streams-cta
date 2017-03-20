xml = eventrate.xml

flink_jar = ../../target/streams-cta-0.0.2-SNAPSHOT-flink-compiled.jar
storm_jar = ../../target/streams-cta-0.0.2-SNAPSHOT-storm-compiled.jar
jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

csv_dir = ../../data/measurements/performance-compare-frameworks

flink_output = $(csv_dir)/performance_flink.csv
storm_output = $(csv_dir)/performance_storm.csv
streams_output = $(csv_dir)/performance_streams-runtime.csv

plot = ../../build/eventrate_vs_runtime.pdf

$(plot): $(flink_output) $(streams_output) $(storm_output) plot_eventrate_by_framework.py
	python plot_eventrate_by_framework.py $(flink_output) $(streams_output) $(storm_output) $(plot) -l flink-runtime -l streams-runtime -l storm-runtime

all: $(plot)



$(flink_output): $(flink_jar) $(xml)
	mkdir -p $(csv_dir)
	java -Xmx6G -jar $(flink_jar) $(xml) -Doutfile=$(flink_output)

$(storm_output): $(storm_jar) $(xml)
	mkdir -p $(csv_dir)
	java -Xmx6G -jar $(storm_jar) $(xml) -Doutfile=$(storm_output)

$(streams_output): $(jar) $(xml)
	mkdir -p $(csv_dir)
	java -Xmx6G -jar $(jar) $(xml) -Doutfile=$(streams_output)

$(flink_jar): ../../pom.xml
	echo "building flink package"
	cd ../../; mvn -P standalone,flink package -DskipTests; cd -

$(storm_jar): ../../pom.xml
	echo "building storm package"
	cd ../../; mvn -P standalone,storm,storm-run package -DskipTests; cd -

$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -
