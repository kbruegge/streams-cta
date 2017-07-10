xml = rta.xml

jar = ../../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/
gammas_test = $(data_dir)/image_features/gammas_test.csv
protons_test = $(data_dir)/image_features/protons_test.csv

gamma_output=$(data_dir)/area/gamma_predictions.csv
proton_output=$(data_dir)/area/proton_predictions.csv

large_sample_mc_meta_information = ../../data/large_sample/mc_production.csv

output_dir=../../build/area
plot = $(output_dir)/trigger_efficiency.pdf
plot_area = $(output_dir)/effective_area.pdf
plot_offical_area = $(output_dir)/official_prod3_area.pdf
mc_meta_information = $(data_dir)/mc_production_meta.csv


raw_data_folder = /fhgfs/users/kbruegge/cta

all:	$(plot) $(plot_area) $(plot_offical_area)

clean:
	rm $(plot) $(plot_area) $(plot_offical_area)


$(plot_area):$(gammas_predicted_output) $(large_sample_mc_meta_information) plot_effective_area.py power_law.py
	export MATPLOTLIBRC=../; python plot_effective_area.py $(gammas_predicted_output) $(large_sample_mc_meta_information)  $(plot_area) --sample_fraction=0.6

$(plot_offical_area): plot_official_irf.py
	export MATPLOTLIBRC=../; python plot_official_irf.py


$(mc_meta_information): add_hardcoded_mc_info.py
	python add_hardcoded_mc_info.py $(raw_data_folder) $(mc_meta_information)


$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -


$(gamma_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dinfile="$(protons_test)"  -Dmodel=$(classification_model) -Doutfile=$(proton_output)
	java  -jar $(jar) $(xml) -Dinfile="$(gammas_test)"  -Dmodel=$(classification_model) -Doutfile=$(gamma_output)
