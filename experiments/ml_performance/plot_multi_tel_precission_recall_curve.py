import click
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from sklearn.metrics import precision_recall_curve


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

    precission, recall, thresholds = precision_recall_curve(y_true, y_score)
    fig, ax = plt.subplots(1)

    ax.plot(
            thresholds,
            recall[:-1],
            label='Recall'
            )

    ax.plot(
            thresholds,
            precission[:-1],
            label='Precission'
            )

    add_rectangles(ax)

    ax.set_ylim([0, 1.05])
    ax.set_xlim([-0.05, 1.05])
    ax.set_xlabel('Prediction Threshold')
    plt.legend()
    plt.savefig(output_file)


if __name__ == '__main__':
    main()
