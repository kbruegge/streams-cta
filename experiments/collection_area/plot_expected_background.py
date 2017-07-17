import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
import power_law
from coordinates import distance_between_estimated_and_mc_direction


@u.quantity_input(area=u.meter**2, event_energies=u.GeV)
def plot_area(bin_edges, event_energies, expectation, area, ax=None, **kwargs):

    event_energies = event_energies.to('TeV').value
    bin_edges = bin_edges.to('TeV').value
    area = area.to('m**2').value

    if not ax:
        _, ax = plt.subplots(1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    H, edges = np.histogram(event_energies, bins=bin_edges)

    area = (H / expectation) * area

    ax.errorbar(
        bin_center,
        area,
        xerr=bin_width * 0.5,
        marker='.',
        linestyle='',
        capsize=0,
        **kwargs,
    )

    ax.set_yscale('log')
    ax.set_xscale('log')
    ax.set_ylabel(r'$Area / \mathrm{m}^2$')
    ax.set_xlabel(r'$E /  \mathrm{TeV}$')

    return ax


@click.command()
@click.argument('triggered_events', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_energy', type=click.INT, default=11, help='number of energy bins')
@click.option(
    '-f',
    '--sample_fraction',
    type=click.FLOAT,
    default=1,
    help='fraction of total monte carlo samples in given file'
)
def main(
        triggered_events,
        mc_production_information,
        outputfile,
        n_energy,
        sample_fraction
):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''

    triggered_events = pd.read_csv(triggered_events)

    # select the source region
    x = triggered_events['stereo:estimated_direction:x'].values * u.m
    y = triggered_events['stereo:estimated_direction:y'].values * u.m
    z = triggered_events['stereo:estimated_direction:z'].values * u.m

    mc_alt = triggered_events['mc:alt'].values * u.rad
    mc_az = triggered_events['mc:az'].values * u.rad

    triggered_events['theta_deg'] = distance_between_estimated_and_mc_direction(x, y, z, mc_alt, mc_az).to('deg')
    triggered_events.rename(index=str,
                            columns={'prediction:signal:mean': 'gammaness',
                                     'mc:energy': 'energy'},
                            inplace=True,
                            )

    # select gamma-like events
    gamma_like_events = triggered_events.query('gammaness >= 0.5')
    signal_events = triggered_events.query('gammaness >= 0.5 & theta_deg <= 0.05')

    # read montecarlo production meta data
    mc = pd.read_csv(mc_production_information)

    proton_file_names = mc.file_names[mc.file_names.str.startswith('proton')]
    files_missing_information = triggered_events[~triggered_events.source_file.isin(proton_file_names)].source_file.unique()

    if len(files_missing_information) > 0:
        print('MC meta information does not match data set. Aborting. Files with missing information:')
        print(np.unique(files_missing_information))


    mc = mc.query(
        'file_names in @triggered_events.source_file.unique()'
    )


    n_simulated_showers = mc.simulated_showers.sum() * sample_fraction
    print('A total of {} showers have been simulated for this sample'.format(n_simulated_showers))

    e_min = mc.energy_min.min() * u.TeV
    e_max = mc.energy_max.max() * u.TeV
    index = mc['index'].mean()
    solid_angle = mc.solidangle_deg.mean() * u.deg
    scatter_radius = mc.scatter_radius_meter.mean() * u.m
    area = np.pi * scatter_radius**2

    mc_spectrum = power_law.MCSpectrum(
        e_min=e_min,
        e_max=e_max,
        index=index,
        total_showers_simulated=n_simulated_showers,
        generation_area=area,
        generator_solid_angle=solid_angle,
    )
    expectation, edges = mc_spectrum.simulated_mc_events_per_bin(bins=30, log=True)

    fig, ax = plt.subplots(1)
    plot_area(
        edges,
        triggered_events['energy'].values * u.TeV,
        expectation,
        area,
        ax=ax,
        label='Cleaned Events'
    )
    plot_area(
        edges,
        gamma_like_events['energy'].values * u.TeV,
        expectation,
        area,
        ax=ax,
        label='Selected "gamma-like" events',
    )
    plot_area(
        edges,
        signal_events['energy'].values * u.TeV,
        expectation,
        area,
        ax=ax,
        label=r'Selected "gamma-like" events with $\Delta \Theta < 0.05$ degrees',
    )

    ax.legend(fontsize=8, framealpha=0.5)
    fig.savefig(outputfile)


if __name__ == '__main__':
    main()
