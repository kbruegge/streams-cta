gammas_predicted_output = ../../data/large_sample/gammas.csv

plot = trigger_efficiency.pdf

mc_meta_information = ../../data/large_sample/mc_production.csv

all:	$(plot)

clean:
	rm $(plot)

$(plot):$(gamma_output) $(mc_meta_information) plot_triggered_events.py power_law.py
	export MATPLOTLIBRC=../; python plot_triggered_events.py $(gammas_predicted_output) $(mc_meta_information)  $(plot)
