import click
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt


@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('output_file', type=click.Path(exists=False, dir_okay=False,))
def main(predicted_gammas, predicted_protons, output_file):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''

    gammas = pd.read_csv(predicted_gammas)
    protons = pd.read_csv(predicted_protons)

    bins = np.linspace(0, 1, 60)

    plt.hist(gammas['prediction:signal:mean'], bins=bins, histtype='step')
    plt.hist(protons['prediction:signal:mean'], bins=bins, histtype='step')

    plt.savefig(output_file)


if __name__ == '__main__':
    main()
