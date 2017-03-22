xml = rta.xml

jar = ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar

csv_dir = ../../data/processed/

proton_output = $(csv_dir)/protons.csv
gamma_output = $(csv_dir)/gammas.csv


all: $(gamma_output) $(proton_output)


$(gamma_output): $(jar) $(xml)
	mkdir -p $(csv_dir)
	java -jar $(jar) $(xml) -Dprefix=gamma*.json.gz -Doutfile=$(gamma_output)


$(proton_output): $(jar) $(xml)
	mkdir -p $(csv_dir)
	java -jar $(jar) $(xml) -Dprefix=proton*.json.gz -Doutfile=$(proton_output)


$(jar): ../../pom.xml
	echo "building streams package"
	cd ../../; mvn -P standalone package -DskipTests; cd -
