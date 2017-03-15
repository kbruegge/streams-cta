flink_jar = ../../target/streams-cta-0.0.2-SNAPSHOT-flink-compiled.jar
jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

csv_dir = ../../data/measurements/performance-compare-frameworks

flink_output = $(csv_dir)/performance_flink.csv
streams_output = $(csv_dir)/performance_streams-runtime.csv

plot = ../../build/eventrate_vs_runtime.pdf

$(plot): $(flink_output) $(streams_output) plot_eventrate_by_framework.py
	python plot_eventrate_by_framework.py $(flink_output) $(streams_output) $(plot) -l flink-runtime -l streams-runtime

all: $(plot)



$(flink_output): $(flink_jar) eventrate.xml
	mkdir -p $(csv_dir)
	java -jar $(flink_jar) eventrate.xml
	mv $(csv_dir)/{performance.csv,performance_flink.csv}


$(streams_output): $(jar) eventrate.xml
	mkdir -p ../../data/measurements/performance-compare-frameworks/
	java -jar $(jar) eventrate.xml
	mv $(csv_dir)/{performance.csv,performance_streams-runtime.csv}

$(flink_jar):
	echo "building flink package"
	cd ../../; mvn -P standalone,flink package -DskipTests; cd -

$(jar):
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -
