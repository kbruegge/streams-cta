import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
from expected_events import power_spectrum


@u.quantity_input(area=u.meter**2)
def plot_area(bin_edges, event_energies, expectation, area, ax=None, **kwargs):

    if not ax:
        _, ax = plt.subplots(1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    H, edges = np.histogram(event_energies, bins=len(bin_center))

    area = H/expectation * area

    ax.errorbar(
                bin_center,
                area.to('m**2').value,
                xerr=bin_width * 0.5,
                marker='.',
                linestyle='',
                capsize=0,
                **kwargs,
                # color='0.25'
            )

    ax.set_yscale('log')
    ax.set_ylabel(r'$Area / \mathrm{m}^2$')
    ax.set_xlabel(r'$\log_{10}(E /  \mathrm{GeV})$')

    return ax


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
    # energy is stored in TeV apparently. lets make it GeV and log it
    triggered_events['log_energy'] = (triggered_events['mc:energy']*1000).apply(np.log10)
    # select gamma-like events
    selected_events = triggered_events[triggered_events['prediction:signal:mean'] >= 0.5]

    # read montecarlo production meta data
    mc = pd.read_csv(mc_production_information)

    n_simulated_showers = mc.query(
        'file_names in @triggered_events.source_file.unique()'
        ) \
        .simulated_showers \
        .sum()

    scatter_radius = mc.query(
        'file_names in @triggered_events.source_file.unique()'
        ) \
        .scatter_radius_meter \
        .mean() * u.m

    area = np.pi * scatter_radius**2

    _, edges = np.histogram(triggered_events['log_energy'], bins=n_energy)
    expectation = power_spectrum(-2, n_simulated_showers, edges)

    fig, ax = plt.subplots(1)
    plot_area(
            edges,
            triggered_events['log_energy'],
            expectation,
            area,
            ax=ax,
            label='Cleaned Events'
        )
    plot_area(
            edges,
            selected_events['log_energy'],
            expectation,
            area,
            ax=ax,
            label='Selected "gamma-like" events',
            )
    ax.legend()
    fig.savefig(outputfile)

if __name__ == '__main__':
    main()
