import click
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from natsort import natsorted


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
    sns.boxplot(result)
    plt.xlabel('number of threads')
    plt.ylabel('events per second and thread')

    plt.savefig(output_file)

if __name__ == "__main__":
    main()
