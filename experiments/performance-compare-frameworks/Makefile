xml = eventrate.xml

flink_jar = ../../target/streams-cta-0.0.3-SNAPSHOT-flink-compiled.jar
storm_jar = ../../target/streams-cta-0.0.3-SNAPSHOT-storm-compiled.jar
jar = ../../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar

build_dir = ../../build/performance-compare-frameworks
csv_dir = $(build_dir)/csv

flink_output = $(csv_dir)/performance_flink.csv
storm_output = $(csv_dir)/performance_storm.csv
streams_output = $(csv_dir)/performance_streams-runtime.csv

plot = $(build_dir)/eventrate_vs_runtime.pdf

$(plot): $(flink_output) $(streams_output) $(storm_output) plot_eventrate_by_framework.py
	python plot_eventrate_by_framework.py $(flink_output) $(streams_output) $(storm_output) $(plot) -l flink-runtime -l streams-runtime -l storm-runtime

all: $(plot)

../../build/performance:
	mkdir -p ../../build/performance/

$(csv_dir):
	mkdir -p $(build_dir)/csv/

$(flink_output): $(flink_jar) $(xml) $(csv_dir)
	java -Xmx6G -jar $(flink_jar) $(xml) -Doutfile=$(flink_output)

$(storm_output): $(storm_jar) $(xml) $(csv_dir)
	java -Xmx6G -jar $(storm_jar) $(xml)  -Doutfile=$(storm_output)

$(streams_output): $(jar) $(xml) $(csv_dir)
	java -Xmx6G -jar $(jar) $(xml)  -Doutfile=$(streams_output)

$(flink_jar): ../../pom.xml
	echo "building flink package"
	cd ../../; mvn -P standalone,flink package -DskipTests; cd -

$(storm_jar): ../../pom.xml
	echo "building storm package"
	cd ../../; mvn -P standalone,storm,storm-run package -DskipTests; cd -

$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -

clean:
	rm $(flink_jar)
	rm $(storm_jar)
	rm $(jar)
	rm $(flink_output)
	rm $(storm_output)
	rm $(streams_output)
