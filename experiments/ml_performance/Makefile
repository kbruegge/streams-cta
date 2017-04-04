xml = write_features.xml
xml_rta = rta.xml
jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/ml_performance

proton_output = $(data_dir)/proton_training.csv
gamma_output = $(data_dir)/gamma_training.csv

model_pmml = $(data_dir)/classifier.pmml
model_pkl = $(data_dir)/classifier.pkl

predictions = $(data_dir)/predictions.hdf
rta_predictions_protons = $(data_dir)/predictions_rta_protons.csv
rta_predictions_gammas = $(data_dir)/predictions_rta_gammas.csv

build_dir = ../../build

plot_single_telescope = $(build_dir)/ml_performance_single_telescope.pdf
plot_multi_tel_auc = $(build_dir)/ml_performance_multi_telescope_auc.pdf
plot_multi_tel_hist = $(build_dir)/ml_performance_multi_telescope_hist.pdf
plot_multi_tel_auc_vs_energy = $(build_dir)/ml_performance_multi_telescope_auc_vs_energy.pdf
plot_multi_tel_precission_recall_curve = $(build_dir)/ml_performance_multi_telescope_recall_vs_cuts.pdf
plot_multi_tel_fpr_tpr = $(build_dir)/ml_performance_multi_telescope_fpr_tpr.pdf

limit = 20000

training_limit = 5000

all: plots 

plots:	$(plot_single_telescope) \
				$(plot_multi_tel)\
				$(plot_multi_tel_hist)\
				$(plot_multi_tel_auc_vs_energy)\
				$(plot_multi_tel_precission_recall_curve)\
				$(plot_multi_tel_fpr_tpr)
clean:
	rm $(gamma_output) $(proton_output) $(predictions) $(model_pmml) $(model_pkl) $(plot) $(plot_multi_tel_auc_vs_energy) $(plot_multi_tel_precission_recall_curve)

clean_jar:
	rm $(jar)


# plot a bunch of performance values for the classifier
$(plot_single_telescope): $(predictions) ./matplotlibrc
	klaas_plot_separator_performance config_separator.yaml $(predictions) $(model_pkl) -o $(plot_single_telescope)

$(plot_multi_tel_auc): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_auc.py
	python plot_multi_tel_auc.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_auc)

$(plot_multi_tel_fpr_tpr): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_fpr_tpr.py
	python plot_multi_tel_fpr_tpr.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_fpr_tpr)

$(plot_multi_tel_hist): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_prediction_hist.py
	python plot_multi_tel_prediction_hist.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_hist)

$(plot_multi_tel_auc_vs_energy): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_auc_vs_energy.py
	python plot_multi_tel_auc_vs_energy.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_auc_vs_energy)

$(plot_multi_tel_precission_recall_curve): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_precission_recall_curve.py
	python plot_multi_tel_precission_recall_curve.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_tel_precission_recall_curve)





$(rta_predictions_protons): $(jar) $(model_pmml)
	java  -jar $(jar) $(xml_rta) -Dprefix="proton*.json.gz" -Dinfolder=../../data/raw -Doutfile=$(rta_predictions_protons) -Dclassifier=$(model_pmml) -Dlimit=$(limit)

$(rta_predictions_gammas): $(jar) $(model_pmml)
	java  -jar $(jar) $(xml_rta) -Dprefix="gamma*.json.gz" -Dinfolder=../../data/raw -Doutfile=$(rta_predictions_gammas) -Dclassifier=$(model_pmml) -Dlimit=$(limit)

$(model_pmml) $(model_pkl) $(predictions): $(proton_output) $(gamma_output) config_separator.yaml
	klaas_train_separation_model config_separator.yaml $(gamma_output) $(proton_output) $(predictions) $(model_pmml)

$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -

$(proton_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=../../data/raw -Doutfile=$(proton_output) -Dlimit=$(training_limit)

$(gamma_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=../../data/raw -Doutfile=$(gamma_output) -Dlimit=$(training_limit)
