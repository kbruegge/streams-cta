import click
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from sklearn.metrics import roc_curve, roc_auc_score
from fact import io


def add_frame(ax, offset=0.1):

    kwargs = {
                'linewidth': 0,
                'edgecolor': 'white',
                'facecolor': 'white',
                'alpha': 0.6,
            }

    left_rect = patches.Rectangle(
        (0 - offset, 0),
        offset,
        1,
        **kwargs
    )
    ax.add_patch(left_rect)


    right_rect = patches.Rectangle(
        (1, 0),
        offset,
        1,
        **kwargs
    )
    ax.add_patch(right_rect)

    top_rect = patches.Rectangle(
        (0 - offset, 1),
        1 + 2 * offset,
        offset,
        **kwargs
    )
    ax.add_patch(top_rect)

    bottom_rect = patches.Rectangle(
        (0 - offset, 0 - offset),
        1 + 2 * offset,
        offset,
        **kwargs
    )
    ax.add_patch(bottom_rect)


@click.command()
@click.argument('cv_predictions_path', type=click.Path(exists=True, dir_okay=False,))
@click.argument('output_file', type=click.Path(exists=False, dir_okay=False,))
def main(cv_predictions_path, output_file):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''

    cv_predictions = io.read_data(cv_predictions_path)

    for name, group in cv_predictions.groupby('telescope_type'):
        y_score = group['probabilities']
        y_true = group['label']

        fpr, tpr, _ = roc_curve(y_true, y_score)
        auc = roc_auc_score(y_true, y_score)

        plt.plot(fpr, tpr, lw=1, label='{} AUC:${:.4f}$'.format(name, auc))

    add_frame(plt.gca())
    plt.legend()
    plt.savefig(output_file)


if __name__ == '__main__':
    main()
