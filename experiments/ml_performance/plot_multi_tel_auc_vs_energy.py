import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import roc_auc_score, recall_score


@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('output_file', type=click.Path(exists=False, dir_okay=False,))
def main(predicted_gammas, predicted_protons, output_file):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''

    n_bins = 10

    gammas = pd.read_csv(predicted_gammas)
    gammas['true_label'] = 1

    protons = pd.read_csv(predicted_protons)
    protons['true_label'] = 0

    df = pd.concat([gammas, protons])
    df['log_energy'] = np.log10(df['mc:energy'] * 1000)
    bin_edges = np.linspace(df.log_energy.min(), df.log_energy.max(), n_bins)
    df['energy_bin'] = pd.cut(df.log_energy, bin_edges)

    total_auc = roc_auc_score(df.true_label, df['prediction:signal:mean'])
    total_recall = recall_score(
        df.true_label, df['prediction:signal:mean'] > 0.5)

    aucs = []
    recalls = []
    for name, group in df.groupby('energy_bin'):
        y_true = group['true_label']
        y_score = group['prediction:signal:mean']
        y_predicted_labels = y_score > 0.5

        auc = roc_auc_score(y_true, y_score)
        recall = recall_score(y_true, y_predicted_labels)

        aucs.append(auc)
        recalls.append(recall)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    plt.errorbar(
                bin_center,
                aucs,
                xerr=bin_width * 0.5,
                marker='.',
                linestyle='',
                capsize=0,
                label='Area under Curve ${:.3f}$'.format(total_auc)
            )

    plt.errorbar(
                bin_center,
                recalls,
                xerr=bin_width * 0.5,
                marker='.',
                linestyle='',
                capsize=0,
                label='Recall ${:.3f}$'.format(total_recall)
            )

    plt.ylim([0, 1.1])
    plt.xlabel(r'$\log_{10}(E /  \mathrm{GeV})$')
    plt.legend()
    plt.savefig(output_file)


if __name__ == '__main__':
    main()
