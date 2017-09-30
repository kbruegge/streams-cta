xml = rta.xml

jar = ../../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/
gammas_predicted_output=$(data_dir)/predictions_rta_gammas_test_sample.csv
protons_predicted_output=$(data_dir)/predictions_rta_protons_test_sample.csv


output_dir=../../build/area
plot_area = $(output_dir)/effective_area.pdf
plot_background = $(output_dir)/expected_background.pdf

mc_meta_information = $(data_dir)/mc_production_meta.csv


raw_data_folder = /fhgfs/users/kbruegge/cta

all:	$(plot_area) $(plot_offical_area) $(mc_meta_information) $(plot_background) $(output_dir)

$(output_dir):
	mkdir -p $(output_dir)

clean:
	rm $(plot_area) $(plot_offical_area) $(plot_background)


$(plot_area):$(gammas_predicted_output) $(large_sample_mc_meta_information) $(mc_meta_information) $(output_dir) plot_effective_area.py power_law.py
	python plot_effective_area.py $(gammas_predicted_output) $(mc_meta_information)  $(plot_area) --sample_fraction=0.6

$(plot_background):$(protons_predicted_output) $(large_sample_mc_meta_information) $(mc_meta_information) $(output_dir) plot_effective_area.py power_law.py
	python plot_expected_background.py $(protons_predicted_output) $(mc_meta_information)  $(plot_background) --sample_fraction=0.6


$(mc_meta_information): add_hardcoded_mc_info.py
	python add_hardcoded_mc_info.py $(raw_data_folder) $(mc_meta_information)
