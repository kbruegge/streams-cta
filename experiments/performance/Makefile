build_dir = ../../build

all: $(build_dir)/performance/eventrate.pdf

$(build_dir)/performance/csv/FINISHED_MEASUREMENT:
	for i in {1..48}; do \
       echo "Using $$i threads" ; \
       java -jar ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar total_eventrate.xml -Dnum_copies="$$i" ; \
    done
	touch $(build_dir)/csv/FINISHED_MEASUREMENT

$(build_dir)/performance/eventrate.pdf: $(build_dir)/performance/csv/FINISHED_MEASUREMENT
	python plot_total_eventrate.py $(build_dir)/performance/csv/*.csv $(build_dir)/performance/eventrate.pdf
