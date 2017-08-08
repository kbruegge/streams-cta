build_dir = ../../build/performance

all: $(build_dir)/eventrate.pdf

$(build_dir)/csv/FINISHED_MEASUREMENT:
	for i in {1..48}; do \
       echo "Using $$i threads" ; \
       java -jar ../../target/streams-cta-0.0.2-SNAPSHOT-stream-compiled.jar total_eventrate.xml -Dnum_copies="$$i" ; \
    done
	touch $(build_dir)/csv/FINISHED_MEASUREMENT

$(build_dir)/eventrate.pdf: $(build_dir)/csv/FINISHED_MEASUREMENT ../matplotlibrc
	export MATPLOTLIBRC=../; python plot_total_eventrate.py $(build_dir)/csv/*.csv $(build_dir)/eventrate.pdf
