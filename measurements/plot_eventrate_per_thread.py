import click
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from natsort import natsorted
import numpy as np


@click.command()
@click.argument('input_files', nargs=-1, type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_files, output_file):

    result = pd.DataFrame()

    for f in input_files:
        df = pd.read_csv(f)
        datarate = df['@datarate']
        num_threads = len(df.groupby('@stream'))

        result['{}'.format(num_threads)] = datarate

    result = result.reindex_axis(natsorted(result.columns), axis=1)

    sns.set_style('darkgrid')
    ax = sns.boxplot(data=result, linewidth=0.6, fliersize=1.5)
    # ax.set_xlim([-1, 49])
    major_ticks = np.arange(3, 49, 4)
    minor_ticks = np.arange(0, 49, 1)

    ax.set_xticks(major_ticks)
    ax.set_xticks(minor_ticks, minor=True)

    labels = np.arange(4, 49, 4)
    ax.set_xticklabels(labels)

    sns.despine()
    plt.xlabel('number of threads')
    plt.ylabel('events per second and thread')

    plt.savefig(output_file)

if __name__ == "__main__":
    main()
