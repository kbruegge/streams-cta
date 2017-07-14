xml_rta = rta_csv.xml
jar = ../../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar


build_dir = ../../build
data_dir = ../../data
# raw_data_dir = /fhgfs/users/kbruegge/cta
plot_output_dir = $(build_dir)/ml_performance/


proton_training_sample = $(data_dir)/image_features/protons_train.csv
gamma_training_sample = $(data_dir)/image_features/gammas_train.csv


proton_test_sample = $(data_dir)/image_features/protons_test.csv
gamma_test_sample = $(data_dir)/image_features/gammas_test.csv

model_pmml = $(build_dir)/models/classifier.pmml
model_pkl = $(build_dir)/models/classifier.pkl

predictions = $(build_dir)/ml_performance/predictions.hdf
rta_predictions_protons = $(data_dir)/predictions_rta_protons_test_sample.csv
rta_predictions_gammas = $(data_dir)/predictions_rta_gammas_test_sample.csv


plot_single_telescope = $(plot_output_dir)/ml_performance_single_telescope.pdf
plot_multi_tel_auc = $(plot_output_dir)/ml_performance_multi_telescope_auc.pdf
plot_multi_tel_hist = $(plot_output_dir)/ml_performance_multi_telescope_hist.pdf
plot_multi_tel_auc_vs_energy = $(plot_output_dir)/ml_performance_multi_telescope_auc_vs_energy.pdf
plot_multi_tel_precission_recall_curve = $(plot_output_dir)/ml_performance_multi_telescope_recall_vs_cuts.pdf
plot_multi_tel_fpr_tpr = $(plot_output_dir)/ml_performance_multi_telescope_fpr_tpr.pdf

all: plots

export MATPLOTLIBRC=../;
plots:  $(plot_single_telescope) \
	$(plot_multi_tel_auc)\
	$(plot_multi_tel_hist)\
	$(plot_multi_tel_auc_vs_energy)\
	$(plot_multi_tel_precission_recall_curve)\
	$(plot_multi_tel_fpr_tpr)
clean:
	rm $(gamma_training_sample) $(proton_training_sample) $(predictions) $(model_pmml) $(model_pkl) $(plot) $(plot_multi_tel_auc_vs_energy) $(plot_multi_tel_precission_recall_curve)


# plot a bunch of performance values for the classifier
$(plot_single_telescope): $(model_pkl) $(predictions) ../matplotlibrc
	klaas_plot_separator_performance config_separator.yaml $(predictions) $(model_pkl) -o $(plot_single_telescope)

$(plot_multi_tel_auc): $(rta_predictions_gammas) $(rta_predictions_protons) ../matplotlibrc plot_multi_tel_auc.py
	export MATPLOTLIBRC=../; python plot_multi_tel_auc.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_auc)

$(plot_multi_tel_fpr_tpr): $(rta_predictions_gammas) $(rta_predictions_protons) ../matplotlibrc plot_multi_tel_fpr_tpr.py
	export MATPLOTLIBRC=../; python plot_multi_tel_fpr_tpr.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_fpr_tpr)

$(plot_multi_tel_hist): $(rta_predictions_gammas) $(rta_predictions_protons) ../matplotlibrc plot_multi_tel_prediction_hist.py
	export MATPLOTLIBRC=../; python plot_multi_tel_prediction_hist.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_hist)

$(plot_multi_tel_auc_vs_energy): $(rta_predictions_gammas) $(rta_predictions_protons) ../matplotlibrc plot_multi_tel_auc_vs_energy.py
	export MATPLOTLIBRC=../; python plot_multi_tel_auc_vs_energy.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_auc_vs_energy)

$(plot_multi_tel_precission_recall_curve): $(rta_predictions_gammas) $(rta_predictions_protons) ../matplotlibrc plot_multi_tel_precission_recall_curve.py
	export MATPLOTLIBRC=../; python plot_multi_tel_precission_recall_curve.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_precission_recall_curve)



$(rta_predictions_protons): $(jar) $(model_pmml)
	java  -jar $(jar) $(xml_rta) -Dinfile=$(proton_test_sample) -Doutfile=$(rta_predictions_protons) -Dclassifier=$(model_pmml)
#
$(rta_predictions_gammas): $(jar) $(model_pmml)
	java  -jar $(jar) $(xml_rta) -Dinfile=$(gamma_test_sample) -Doutfile=$(rta_predictions_gammas) -Dclassifier=$(model_pmml)

$(model_pmml) $(model_pkl) $(predictions): $(proton_training_sample) $(gamma_training_sample) config_separator.yaml
	klaas_train_separation_model config_separator.yaml $(gamma_training_sample) $(proton_training_sample) $(predictions) $(model_pmml)



#
$(jar): ../../pom.xml $(shell find ../../src/ -type f)
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -

# $(proton_training_sample): $(jar) $(xml)
# 	mkdir -p $(data_dir)
# 	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=$(raw_data_dir) -Doutfile=$(proton_training_sample) -Dlimit=$(training_limit)
#
# $(gamma_training_sample): $(jar) $(xml)
# 	mkdir -p $(data_dir)
# 	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=$(raw_data_dir) -Doutfile=$(gamma_training_sample) -Dlimit=$(training_limit)
