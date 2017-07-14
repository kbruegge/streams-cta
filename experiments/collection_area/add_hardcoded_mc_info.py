import click
import os
import pandas as pd


@click.command()
@click.argument('input_folder', type=click.Path(exists=True, file_okay=False))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_folder, output_file):
    '''
    The INPUT_FOLDER argument specifies the path to the json.gz files which have been
    converted from simtel files. This script assumes standard Prod3 settings
    and stores production settings in the csv file specified by OUTPUT_FILE argument.
    '''

    # check whether extension is csv
    ext = os.path.splitext(output_file)[1]
    if not ext == '.csv':
        print('Nope. output_file has to be csv')
        return

    protons = filter(lambda f: f.startswith('proton'), map(
        lambda s: s.replace('.json.gz', ''), os.listdir(input_folder)))
    gammas = filter(lambda f: f.startswith('gamma'), map(
        lambda s: s.replace('.json.gz', ''), os.listdir(input_folder)))

    gammas = {s: {'index': -2.0,
                  'simulated_showers': 20000 * 10,  # reuse is == 10 for prod3
                  'scatter_radius_meter': 2500.0,
                  'energy_min': 0.003,
                  'energy_max': 330,
                  'solidangle_deg': None,
                  } for s in gammas}

    protons = {s: {'index': -2.0,
                   'simulated_showers': 50000 * 20,  # reuse is 20 for protons in prod3
                   'scatter_radius_meter': 3000.0,
                   'energy_min': 0.004,
                   'energy_max': 600,
                   'solidangle_deg': 6,
                   } for s in protons}

    entries = dict(gammas, **protons)
    df = pd.DataFrame.from_dict(entries, orient='index')
    df.to_csv(output_file, index_label='file_names')


if __name__ == '__main__':
    main()
