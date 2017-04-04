import click
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from sklearn.metrics import roc_curve


def add_rectangles(ax, offset=0.1):

    kwargs = {
                'linewidth': 1,
                'edgecolor': 'white',
                'facecolor': 'white',
                'alpha': 0.4,
            }

    rect = patches.Rectangle(
                        (0 - offset, 0),
                        0 + offset,
                        1 + offset,
                        **kwargs
                    )
    ax.add_patch(rect)

    rect = patches.Rectangle(
                        (1, 0),
                        0 + offset,
                        1 + offset,
                        **kwargs
                    )
    ax.add_patch(rect)

    rect = patches.Rectangle(
                        (0, 1),
                        1 + offset,
                        0 + offset,
                        **kwargs
                    )
    ax.add_patch(rect)


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
    gammas['true_label'] = 1

    protons = pd.read_csv(predicted_protons)
    protons['true_label'] = 0

    df = pd.concat([gammas, protons])

    y_score = df['prediction:signal:mean']
    y_true = df['true_label']

    fpr, tpr, thresholds = roc_curve(y_true, y_score)

    plt.plot(thresholds, tpr, lw=1, label='True Positive Rate')
    plt.plot(thresholds, fpr, lw=1, label='False Positive Rate')
    plt.axvline(0.5, ls='--', color='0.8', lw=1)
    plt.xlabel('Prediction Threshold')
    plt.legend(loc='upper right')

    add_rectangles(plt.gca())

    plt.savefig(output_file)


if __name__ == '__main__':
    main()
