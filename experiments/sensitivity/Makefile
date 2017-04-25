gammas_predicted_output = ../../data/large_sample/gammas.csv

trigger_efficiency = ../../build/trigger_efficiency.pdf
crab_efficiency = ../../build/crab_efficiency.pdf

mc_meta_information = ../../data/large_sample/mc_production.csv

all:	$(trigger_efficiency) $(crab_efficiency)

clean:
	rm $(trigger_efficiency)

$(trigger_efficiency):$(gamma_output) $(mc_meta_information) plot_trigger_efficiency.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_trigger_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(trigger_efficiency)

$(crab_efficiency):$(gamma_output) $(mc_meta_information) plot_crab_efficiency.py power_law.py cta_io.py
	export MATPLOTLIBRC=../; python plot_crab_efficiency.py $(gammas_predicted_output) $(mc_meta_information)  $(crab_efficiency)
