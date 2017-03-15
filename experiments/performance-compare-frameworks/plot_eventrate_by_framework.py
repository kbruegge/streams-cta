import click
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns


@click.command()
@click.argument('input_files', nargs=-1, type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
@click.option('--label', '-l', multiple=True, help='the labels of the groups')
def main(input_files, output_file, label):

    result = pd.DataFrame()
    print(label)
    if not label:
        label = list(range(1, len(input_files) + 1))

    for i, f in enumerate(input_files):
        df = pd.read_csv(f)
        # drop first line
        df = df[1:]
        datarate = df['@datarate']
        result['{}'.format(label[i])] = datarate

    # result = result.reindex_axis(natsorted(result.columns), axis=1)

    sns.set_style('darkgrid')
    with sns.color_palette(['#3697f1', '#ff8600', ]):
        sns.violinplot(data=result, linewidth=1.2, fliersize=1)

    sns.despine()
    # plt.xlabel('number of threads')
    plt.ylabel('events per second')

    plt.savefig(output_file)

if __name__ == "__main__":
    main()
