
SST=../../data/image_features/gammas_train_sst.hdf
MST=../../data/image_features/gammas_train_mst.hdf
LST=../../data/image_features/gammas_train_lst.hdf

SST_MODEL=model_sst.pkl
MST_MODEL=model_mst.pkl
LST_MODEL=model_lst.pkl

all: $(SST_MODEL) $(MST_MODEL) $(LST_MODEL)

$(SST):../../data/image_features/gammas_train.csv
	klaas_apply_cuts sst_selection.yml ../../data/image_features/gammas_train.csv $(SST) --hdf-style pandas -k events

$(MST):../../data/image_features/gammas_train.csv
	klaas_apply_cuts mst_selection.yml ../../data/image_features/gammas_train.csv $(MST) --hdf-style pandas -k events

$(LST):../../data/image_features/gammas_train.csv
	klaas_apply_cuts lst_selection.yml ../../data/image_features/gammas_train.csv $(LST) --hdf-style pandas -k events


$(SST_MODEL): $(SST)
	klaas_train_energy_regressor config_regressor.yaml ../../data/image_features/gammas_train_sst.hdf predictions_sst.hdf model_sst.pkl -k events
	klaas_plot_regressor_performance config_regressor.yaml predictions_sst.hdf model_sst.pkl -o plots_sst.pdf

$(MST_MODEL): $(MST)
	klaas_train_energy_regressor config_regressor.yaml ../../data/image_features/gammas_train_mst.hdf predictions_mst.hdf model_mst.pkl
	klaas_plot_regressor_performance config_regressor.yaml predictions_mst.hdf model_mst.pkl -o plots_mst.pdf

$(LST_MODEL): $(LST)
	klaas_train_energy_regressor config_regressor.yaml ../../data/image_features/gammas_train_lst.hdf predictions_lst.hdf model_lst.pkl
	klaas_plot_regressor_performance config_regressor.yaml predictions_lst.hdf model_lst.pkl -o plots_lst.pdf
