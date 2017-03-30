xml = write_features.xml
xml_rta = rta.xml
jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

data_dir = ../../data/ml_performance

proton_output = $(data_dir)/proton_training.csv
gamma_output = $(data_dir)/gamma_training.csv

model_pmml = $(data_dir)classifier.pmml
model_pkl = $(data_dir)classifier.pkl

predictions = $(data_dir)/predictions.hdf
rta_predictions_protons = $(data_dir)/predictions_rta_protons.csv
rta_predictions_gammas = $(data_dir)/predictions_rta_gammas.csv

plot_single_telescope = ml_performance_single_telescope.pdf
plot_multi_telescope = ml_performance_multi_telescope.pdf
plot_multi_telescope_hist = ml_performance_multi_telescope_hist.pdf

limit = 2000

all: $(plot_single_telescope) $(plot_multi_telescope) $(plot_multi_telescope_hist)

clean:
	rm $(gamma_output) $(proton_output) $(predictions) $(model_pmml) $(model_pkl) $(plot)

clean_jar:
	rm $(gamma_output) $(proton_output) $(predictions) $(model_pmml) $(model_pkl) $(plot) $(jar)



$(plot_single_telescope): $(predictions) ./matplotlibrc
	klaas_plot_separator_performance config_separator.yaml $(predictions) $(model_pkl) -o $(plot_single_telescope)


$(plot_multi_telescope): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_auc.py
	python plot_multi_tel_auc.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_telescope)

$(plot_multi_telescope_hist): $(rta_predictions_gammas) $(rta_predictions_protons) ./matplotlibrc plot_multi_tel_prediction_hist.py
	python plot_multi_tel_prediction_hist.py  $(rta_predictions_gammas) $(rta_predictions_protons) $(plot_multi_telescope_hist)



$(rta_predictions_protons): $(jar) $(model_pmml)
	java  -jar $(jar) $(xml_rta) -Dprefix="proton*.json.gz" -Dinfolder=../../data/raw -Doutfile=$(rta_predictions_protons) -Dclassifier=$(model_pmml) -Dlimit=$(limit)

$(rta_predictions_gammas): $(jar)
	java  -jar $(jar) $(xml_rta) -Dprefix="gamma*.json.gz" -Dinfolder=../../data/raw -Doutfile=$(rta_predictions_gammas) -Dclassifier=$(model_pmml) -Dlimit=$(limit)

$(model_pmml) $(model_pkl) $(predictions): $(proton_output) $(gamma_output)
	klaas_train_separation_model config_separator.yaml $(gamma_output) $(proton_output) $(predictions) $(model_pmml)

$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -

$(proton_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=../../data/raw -Doutfile=$(proton_output) -Dlimit=$(limit)

$(gamma_output): $(jar) $(xml)
	mkdir -p $(data_dir)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=../../data/raw -Doutfile=$(gamma_output) -Dlimit=$(limit)
