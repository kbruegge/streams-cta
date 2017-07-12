SHELL=/bin/bash

jar = ../target/streams-cta-0.0.3-SNAPSHOT-stream-compiled.jar
xml = process_data.xml
raw_data_folder = /fhgfs/users/kbruegge/cta

gamma_output = ../data/image_features/gammas.csv
proton_output = ../data/image_features/protons.csv

proton_test = ../data/image_features/protons_test.csv
gamma_test = ../data/image_features/gammas_test.csv

proton_train = ../data/image_features/protons_train.csv
gamma_train = ../data/image_features/gammas_train.csv

all: $(proton_test) $(proton_train) $(gamma_test) $(gamma_train)

$(proton_test) $(proton_train): $(proton_output)
	klaas_split_data $(proton_output) protons -n test -f 0.6 -n train -f 0.4
	
$(gamma_test) $(gamma_train): $(gamma_output)
	klaas_split_data $(gamma_output) gammas -n test -f 0.6 -n train -f 0.4


$(gamma_output): $(jar) $(xml)
	java  -jar $(jar) $(xml) -Dprefix="gamma*.json.gz" -Dfolder=$(raw_data_folder) -Doutfile=$(gamma_output)


$(proton_output): $(jar) $(xml)
	java  -jar $(jar) $(xml) -Dprefix="proton*.json.gz" -Dfolder=$(raw_data_folder) -Doutfile=$(proton_output)

$(jar): ../pom.xml $(shell find ../src -type f)
	echo "building streams package in top level"
	cd ../; mvn -P standalone package -DskipTests; cd -

