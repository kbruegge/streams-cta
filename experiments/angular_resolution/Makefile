
gammas_test=../../data/predictions_rta_gammas_test_sample.csv

dir = ../../build/angular_resolution

impact = $(dir)/impact_estimator.pdf
resolution = $(dir)/angular_resolution.pdf
resolution_vs_triggered = $(dir)/angular_resolution_vs_triggered.pdf
resolution_vs_energy = $(dir)/angular_resolution_vs_energy.pdf

all: $(dir) $(impact) $(resolution) $(resolution_vs_triggered) $(resolution_vs_energy)

$(dir):
	mkdir -p  $(dir)

$(impact): plot_impact_estimator.py $(dir)
	python plot_impact_estimator.py $(gammas_test) $(impact)

$(resolution): plot_angular_resolution.py $(dir)
	python plot_angular_resolution.py $(gammas_test) $(resolution)

$(resolution_vs_triggered): plot_angular_resolution_vs_triggered_telescopes.py $(dir)
	python plot_angular_resolution_vs_triggered_telescopes.py $(gammas_test) $(resolution_vs_triggered)

$(resolution_vs_energy): plot_angular_resolution_vs_energy.py $(dir)
	python plot_angular_resolution_vs_energy.py $(gammas_test) $(resolution_vs_energy)
