xml = triggered_events.xml

jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/area

proton_output = $(data_dir)/proton_triggered.csv
gamma_output = $(data_dir)/gamma_triggered.csv

plot = triggered_events.pdf
mc_meta_information = $(data_dir)/mc_production.csv

raw_data_folder = ../../data/raw

all: $(plot)

$(plot):$(gamma_output) $(mc_meta_information)
	python plot_triggered_events.py $(gamma_output) $(mc_meta_information)  $(plot)

$(mc_meta_information):
	python add_hardcoded_mc_info.py $(raw_data_folder) $(mc_meta_information)

$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -
#
# $(proton_output): $(jar) $(xml)
# 	mkdir -p $(data_dir)
# 	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=../../data/raw -Doutfile=$(proton_output)

$(gamma_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=../../data/raw -Doutfile=$(gamma_output)
