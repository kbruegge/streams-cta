xml = triggered_events.xml
rta_xml = rta.xml

jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/area

proton_output = $(data_dir)/proton_triggered.csv
gamma_output = $(data_dir)/gamma_triggered.csv

gammas_predicted_output = ../../data/gammas.csv

plot = ../../build/trigger_efficiency.pdf
plot_area = ../../build/effective_area.pdf
mc_meta_information = $(data_dir)/mc_production.csv

raw_data_folder = ../../data/raw

all: $(plot) $(plot_area)

$(plot):$(gamma_output) $(mc_meta_information) plot_triggered_events.py
	python plot_triggered_events.py $(gamma_output) $(mc_meta_information)  $(plot)

$(plot_area):$(gammas_predicted_output) $(mc_meta_information) plot_effective_area.py
	python plot_effective_area.py $(gammas_predicted_output) $(mc_meta_information)  $(plot_area)


$(mc_meta_information): add_hardcoded_mc_info.py
	python add_hardcoded_mc_info.py $(raw_data_folder) $(mc_meta_information)


$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -


$(gamma_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=../../data/raw -Doutfile=$(gamma_output)
