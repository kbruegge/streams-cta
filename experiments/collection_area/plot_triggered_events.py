import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
from expected_events import power_spectrum


def plot(bin_edges, energy, expectation):
    fig, (ax, ax2) = plt.subplots(2, 1)
    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    ax.set_yscale('log')
    ax.errorbar(bin_center, expectation, xerr=bin_width *
                0.5, marker='.', linestyle='', capsize=0, label='Simulated Showers')
    H, _, _ = ax.hist(energy, bins=len(bin_center), label='Triggered Events')

    ax.legend()

    ax2.errorbar(bin_center, H / expectation, xerr=bin_width *
                 0.5, marker='.', linestyle='', capsize=0, color='0.25')

    ax2.set_ylabel('Efficiency')
    ax2.set_xlabel(r'$\log_{10}(E /  \mathrm{GeV})$')

    return fig, (ax, ax2)


@click.command()
@click.argument('triggered_events', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_energy', type=click.INT, default=11, help='energy bins')
def main(
        triggered_events,
        mc_production_information,
        outputfile,
        n_energy,
        ):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''
    triggered_events = pd.read_csv(triggered_events)
    mc = pd.read_csv(mc_production_information)

    simulated_showers = mc.query(
        'file_names in @triggered_events.source_file.unique()'
        ) \
        .simulated_showers \
        .sum()

    # energy is stored in TeV apparently. lets make it GeV and log it
    energy = (triggered_events['mc:energy'] * 1000).apply(np.log10)

    _, edges = np.histogram(energy, bins=n_energy)

    expectation = power_spectrum(-2, simulated_showers, edges)

    fig, _ = plot(edges, energy, expectation)
    fig.savefig(outputfile)

if __name__ == '__main__':
    main()
