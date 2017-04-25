gammas_predicted_output = ../../data/large_sample/gammas.csv
protons_predicted_output = ../../data/large_sample/protons.csv

trigger_efficiency = ../../build/trigger_efficiency.pdf
crab_efficiency = ../../build/crab_efficiency.pdf
theta = ../../build/theta.pdf
sens = ../../build/sensitivity.pdf

mc_meta_information = ../../data/large_sample/mc_production.csv

all:	$(trigger_efficiency) $(crab_efficiency) $(theta) $(sens)

clean:
	rm $(trigger_efficiency)

$(trigger_efficiency):$(gammas_predicted_output) $(mc_meta_information) plot_trigger_efficiency.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_trigger_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(trigger_efficiency)

$(crab_efficiency):$(gammas_predicted_output) $(mc_meta_information) plot_crab_efficiency.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_crab_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(crab_efficiency)

$(theta):$(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information) plot_theta.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_theta.py $(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information)  $(theta)

$(sens):$(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information) plot_sensitivity.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_sensitivity.py $(gammas_predicted_output) $(protons_predicted_output) $(mc_meta_information)  $(sens) -n 7 -i 50
