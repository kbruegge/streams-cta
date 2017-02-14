images_path = ../src/main/resources/images.json
array_path = ../src/main/resources/hexmap/cta_array_definition.json

simtel_path = ../../gamma_test_large.simtel.gz


all: $(array_path) ../src/main/resources/hexmap/cta_camera_definitions.json $(images_path)

array $(array_path): array_definitions.py
	python ./array_definitions.py $(simtel_path) $(array_path)

cameras ../src/main/resources/hexmap/cta_camera_definitions.json: camera_definitions.py
	python ./camera_definitions.py $(simtel_path) ../src/main/resources/hexmap/cta_camera_definitions.json

raw $(images_path): convert_raw_data.py
	python ./convert_raw_data.py $(simtel_path) $(images_path)
	gzip $(images_path)
