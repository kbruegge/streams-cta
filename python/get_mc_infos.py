import click
import subprocess
import numpy as np
import os
import pandas as pd


def parse_header(header_lines):

    s = header_lines
    simulated_events = map(
                lambda e: int(e.split(b':')[1]),

                filter(
                    lambda l: l.startswith(b'  Number of showers to be simulated'),
                    s,
                ),
            )

    shower_multiplicity = map(
                lambda e: int(e.split(b':')[1]),

                filter(
                    lambda l: l.startswith(b'  Number of arrays per shower'),
                    s,
                ),
            )

    spectral_indeces = map(
                lambda e: float(e.split(b':')[1]),

                filter(
                    lambda l: l.startswith(b'  Spectral index:'),
                    s,
                ),
            )

    return np.array(list(simulated_events)), \
        np.array(list(shower_multiplicity)), \
        np.array(list(spectral_indeces))


@click.command()
@click.argument('input_files', nargs=-1, type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_files, output_file):
    '''
    The INPUT_FILES argument specifies the path to simtel files. This script reads the
    MC header using the provided binary of read_hess and stores production settings in
    the file specified by OUTPUT_FILE argument.
    '''

    # check whether extension is csv
    ext = os.path.splitext(output_file)[1]
    if not ext == '.csv':
        print('Nope. output_file has to be csv')
        return

    d = {}
    for f in input_files:
        cmd = ['../binaries/read_hess', '--max-events', '1', '-s', f]
        header_lines = subprocess.run(cmd, stdout=subprocess.PIPE).stdout.splitlines()

        events, multiplicity, indeces = parse_header(header_lines)

        # check that we only simulate with one index
        if len(np.unique(indeces)) > 1:
            print('We only support one spectral index per file')
            return

        index = indeces[0]

        simulated_showers = np.sum(events * multiplicity)
        key = os.path.basename(f).replace('.simtel.gz', '').strip()
        d[key] = {'index': index, 'simulated_showers': simulated_showers}

    df = pd.DataFrame.from_dict(d, orient='index')
    df.to_csv(output_file)

    # l = subprocess.run(['ls'], stdout=subprocess.PIPE)
    # print(l)



if __name__ == '__main__':
    main()
