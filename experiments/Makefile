SHELL=/bin/bash

jar = ../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar
xml = process_data.xml
raw_data_folder = ../../data/raw

gamma_output = ../data/image_features/gammas.csv
proton_output = ../data/image_features/protons.csv

proton_test = ../data/image_features/protons_test.csv
gamma_test = ../data/image_features/gammas_test.csv

proton_train = ../data/image_features/protons_train.csv
gamma_train = ../data/image_features/gammas_train.csv


all: $(proton_test) $(gamma_test) $(gamma_train) $(proton_train)
	@for a in $$(ls); do \
		if [ -d $$a ]; then \
			echo "processing folder $$a"; \
			$(MAKE) -C $$a; \
		fi; \
	done;
	@echo "Done!"


$(proton_test) $(gamma_test) $(gamma_train) $(proton_train): $(proton_output) $(gamma_output)
	klaas_split_data $(proton_output) background -n test -f 0.6 -n train -f 0.4
	klaas_split_data $(gamma_output) signal -n test -f 0.6 -n train -f 0.4


$(gamma_output): $(jar) $(xml)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=$(raw_data_folder) -Doutfile=$(gamma_output)


$(proton_output): $(jar) $(xml)
	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=$(raw_data_folder) -Doutfile=$(proton_output)


$(jar): ../pom.xml ../src
	echo "building streams package"
	cd ../; mvn -P standalone package -DskipTests; cd -
