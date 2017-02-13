all: ../src/main/resources/hexmap/cta_array_definition.json ../src/main/resources/hexmap/cta_camera_definitions.json

array ../src/main/resources/hexmap/cta_array_definition.json: array_definitions.py
	python ./array_definitions.py ../../gamma_test_large.simtel.gz ../src/main/resources/hexmap/cta_array_definition.json

cameras ../src/main/resources/hexmap/cta_camera_definitions.json: array_definitions.py
	python ./camera_definitions.py ../../gamma_test_large.simtel.gz ../src/main/resources/hexmap/cta_camera_definitions.json
