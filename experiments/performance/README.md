## Speed Measurement

The `xml` in this folder runs the entire analysis from raw calibrated images to
high level eventlist with estimated energy and classification. It saves the
datarates to a csv file. The filename of the csv file contains the  umber of threads that
were used.

Total event rates and event rate per thread can be plotted by calling
the two python scripts.

```
▶ python experiments/performance/plot_total_eventrate.py --help
Usage: plot_total_eventrate.py [OPTIONS] [INPUT_FILES]... OUTPUT_FILE

  This takes multiple csv files as INPUT_FILES and produces a plot at the
  given OUTPUT_FILE path. The csv files are expected to contain at least the
  '@stream' and '@datarate' columns.

Options:
  --help  Show this message and exit.

```
