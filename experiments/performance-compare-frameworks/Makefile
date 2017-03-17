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



$(flink_output): $(flink_jar) eventrate_distributed.xml
	mkdir -p $(csv_dir)
	java -Xmx6G -jar $(flink_jar) eventrate_kai.xml
	mv $(csv_dir)/{performance.csv,performance_flink.csv}

$(storm_output): $(storm_jar) eventrate_distributed.xml
	mkdir -p $(csv_dir)
	java -Xmx6G -jar $(storm_jar) eventrate_kai.xml
	mv $(csv_dir)/{performance.csv,performance_storm.csv}

$(streams_output): $(jar) eventrate.xml
	mkdir -p ../../data/measurements/performance-compare-frameworks/
	java -Xmx6G -jar $(jar) eventrate_kai.xml
	mv $(csv_dir)/{performance.csv,performance_streams-runtime.csv}

$(flink_jar):
	echo "building flink package"
	cd ../../; mvn -P standalone,flink package -DskipTests; cd -

$(storm_jar):
	echo "building storm package"
	cd ../../; mvn -P standalone,storm,storm-run package -DskipTests; cd -

$(jar):
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -
