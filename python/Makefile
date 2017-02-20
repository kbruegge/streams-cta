images_path = ../src/main/resources/images.json
array_path = ../src/main/resources/hexmap/cta_array_definition.json
camera_path = ../src/main/resources/hexmap/cta_camera_definitions.json

simtel_path = ../../gamma_test_large.simtel.gz


all: $(array_path) $(camera_path) $(images_path)

array $(array_path): array_definitions.py
	python ./array_definitions.py $(simtel_path) $(array_path)

cameras $(camera_path): camera_definitions.py
	python ./camera_definitions.py $(simtel_path) $(camera_path)

raw $(images_path): convert_raw_data.py
	python ./convert_raw_data.py $(simtel_path) $(images_path)
	gzip $(images_path)

clean:
	rm $(array_path)
	rm $(camera_path)
