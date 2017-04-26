import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
import power_law


@u.quantity_input(event_energies=u.GeV)
def plot(bin_edges, event_energies, expectation):

    event_energies = event_energies.to('TeV').value
    fig, (ax, ax2) = plt.subplots(2, 1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    ax.errorbar(bin_center, expectation, xerr=bin_width *
                0.5, marker='.', linestyle='', capsize=0, label='Simulated Showers')
    H, _, _ = ax.hist(event_energies, bins=bin_edges, label='Triggered Events')
    ax.set_yscale('log')
    ax.set_xscale('log')
    ax.legend()

    # plot efficiency in lower axis
    ax2.errorbar(bin_center, H / expectation, xerr=bin_width *
                 0.5, marker='.', linestyle='', capsize=0, color='0.25')

    ax2.set_ylabel('Efficiency')
    ax2.set_xlabel(r'$\log_{10}(E /  \mathrm{GeV})$')
    ax2.set_xscale('log')

    return fig, (ax, ax2)


def get_mc_information(df, df_mc):
    if len(df.source_file.unique()) > len(df_mc.file_names):
        print('Shits going down yo')

    mcs_for_events = df_mc.query(
        'file_names in @df.source_file.unique()'
    )

    n_simulated_showers = mcs_for_events.simulated_showers.sum()

    e_min = mcs_for_events.energy_min.min() * u.TeV
    e_max = mcs_for_events.energy_max.max() * u.TeV

    if len(mcs_for_events.energy_min.unique()) > 1:
        print('different enregie ranges simulated? screw dat!')

    if len(mcs_for_events.scatter_radius_meter.unique()) > 1:
        print('different scatter radii simulated? screw dat!')

    area = np.pi * mcs_for_events.scatter_radius_meter.min()**2 * u.m**2

    return n_simulated_showers, e_min, e_max, area


@click.command()
@click.argument('gamma_file', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_energy', type=click.INT, default=11, help='energy bins')
def main(
        gamma_file,
        mc_production_information,
        outputfile,
        n_energy,
        ):
    '''
    Plot the event distributions from the triggered gammas given in the
    PREDICTED_EVENTS input file.
    '''
    gammas = pd.read_csv(gamma_file)
    df_mc = pd.read_csv(mc_production_information)

    n_simulated_showers, e_min, e_max, area = get_mc_information(gammas, df_mc)

    mc = power_law.MCSpectrum(
        e_min=e_min,
        e_max=e_max,
        total_showers_simulated=n_simulated_showers,
        generation_area=area,
        generator_solid_angle=6 * u.deg
    )

    expected_events, edges = mc.expected_events_for_bins(
            e_min=e_min,
            e_max=e_max,
            area=area,
            t_obs=1*u.s,
            solid_angle=6*u.deg,
            bins=30
        )

    energy = gammas['mc:energy'].values * u.TeV

    fig, _ = plot(edges.value, energy, expected_events)
    fig.savefig(outputfile)

if __name__ == '__main__':
    main()
