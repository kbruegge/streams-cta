images_path = ../src/main/resources/images.json

simtel_path = ../../gamma_test_large.simtel.gz


all: ../src/main/resources/hexmap/cta_array_definition.json ../src/main/resources/hexmap/cta_camera_definitions.json $(images_path)

array ../src/main/resources/hexmap/cta_array_definition.json: array_definitions.py
	python ./array_definitions.py $(simtel_path) ../src/main/resources/hexmap/cta_array_definition.json

cameras ../src/main/resources/hexmap/cta_camera_definitions.json: camera_definitions.py
	python ./camera_definitions.py $(simtel_path) ../src/main/resources/hexmap/cta_camera_definitions.json

raw $(images_path): convert_raw_data.py
	python ./convert_raw_data.py $(simtel_path) $(images_path)
	gzip $(images_path)
