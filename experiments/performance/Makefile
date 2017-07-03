all: build/eventrate.pdf

build/csv/FINISHED_MEASUREMENT:
	for i in {1..4}; do \
       echo "Using $$i threads" ; \
       java -jar ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar total_eventrate.xml -Dnum_copies="$$i" ; \
    done
	touch build/csv/FINISHED_MEASUREMENT

build/eventrate.pdf: build/csv/FINISHED_MEASUREMENT
	python plot_total_eventrate.py build/csv/*.csv build/eventrate.pdf
