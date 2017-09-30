data_dir = ../../data
gammas_predicted_output=$(data_dir)/predictions_rta_gammas_test_sample.csv
protons_predicted_output=$(data_dir)/predictions_rta_protons_test_sample.csv

build_dir = ../../build/sensitivity
trigger_efficiency = $(build_dir)/trigger_efficiency.pdf
crab_efficiency = $(build_dir)/crab_efficiency.pdf
theta = $(build_dir)/theta.pdf
sens = $(build_dir)/sensitivity.pdf
sens_binned = $(build_dir)/sensitivity_binned.pdf
mc_meta_information = ../../data/mc_production_meta.csv

all:	$(build_dir) $(trigger_efficiency) $(crab_efficiency) $(theta) $(sens)

clean:
	rm $(trigger_efficiency) $(crab_efficiency) $(theta) $(sens)

$(build_dir):
	mkdir -p $(build_dir)


$(trigger_efficiency):$(gammas_predicted_output) $(mc_meta_information) plot_trigger_efficiency.py power_law.py cta_io.py
	 python plot_trigger_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(trigger_efficiency) -s 0.6

$(crab_efficiency):$(gammas_predicted_output) $(mc_meta_information) plot_crab_efficiency.py power_law.py cta_io.py
	 python plot_crab_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(crab_efficiency)

$(theta):$(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information) plot_theta.py power_law.py cta_io.py
	 python plot_theta.py $(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information)  $(theta) -s 0.6

$(sens):$(protons_predicted_output) $(gammas_predicted_output) $(mc_meta_information) plot_sensitivity.py power_law.py cta_io.py
	 python plot_sensitivity.py $(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information)  $(sens) -n 25 -s 0.6

$(sens_binned):$(protons_predicted_output) $(gammas_predicted_output) $(mc_meta_information) plot_sensitivity_new.py power_law.py cta_io.py
	 python plot_sensitivity_new.py $(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information)  $(sens_binned) -n 25 -s 0.6
