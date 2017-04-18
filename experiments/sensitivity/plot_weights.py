import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
import power_law


# def plot_sensitivity(bin_edges, sensitivity, ax=None, error=None, **kwargs):
#
#     if not ax:
#         _, ax = plt.subplots(1)
#
#     bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
#     bin_width = np.diff(bin_edges)
#
#     ax.errorbar(
#         bin_center,
#         sensitivity,
#         xerr=bin_width * 0.5,
#         yerr=error,
#         marker='.',
#         linestyle='',
#         capsize=0,
#         **kwargs,
#     )
#
#     ax.set_yscale('log')
#     # ax.set_ylabel(r'$Area / \mathrm{m}^2$')
#     ax.set_xlabel(r'$\log_{10}(E /  \mathrm{TeV})$')
#
#     return ax
#

def read_events(path_to_file, source_alt=20, source_az=0):
    df = pd.read_csv(path_to_file).dropna()
    df.rename(index=str,
              columns={
                  'prediction:signal:mean': 'gammaness',
                  'mc:energy': 'energy'
              },
              inplace=True,
              )

    df = df[df.source_file.str.contains('_20deg_0deg')]
    return df


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


# @click.command()
# @click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
# @click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
# @click.argument('mc_production_information', type=click.Path(exists=True))
# @click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
# @click.option('-n', '--n_bins', type=click.INT, default=30, help='theta bin')
def main():

    # df_mc = pd.read_csv(mc_production_information)

    # gammas = read_events(predicted_gammas)

    # n_simulated_showers, e_min, e_max, area = get_mc_information(gammas, df_mc)
    # protons = read_events(predicted_protons)

    # selected_protons = protons.query('gammaness >= 0.5')

    # selected_gammas = gammas.query('gammaness >= 0.5')

    gammas = pd.DataFrame()
    e_min = 0.01*u.TeV
    e_max = 100.*u.TeV
    area = 5*u.km**2
    N = 100000

    gammas['energy'] = np.random.uniform(e_min.value, e_max.value, N*0.1)

    crab = power_law.CrabSpectrum()

    gammas['weight'] = crab.weight(gammas.energy.values * u.TeV, e_min, e_max, area, N)

    events, edges = crab.expected_events_for_bins(e_min, e_max, 1 * u.km**2, t_obs=1*u.s, log=True)

    bin_center = 0.5 * (edges[:-1] + edges[1:])
    bin_width = np.diff(edges)

    plt.errorbar(bin_center.value, events, xerr=bin_width.value*0.5, linestyle='', marker='.',)
    plt.hist(gammas.energy, bins=edges, weights=gammas.weight)
    plt.yscale('log')
    plt.xscale('log')

    plt.show()
    # on_events = selected_gammas.query('theta_deg_squared < 0.005').copy()
    # off_events = selected_protons.query('theta_deg_squared < 0.005').copy()
    #
    # on_events.energy = np.log10(on_events.energy)
    # off_events.energy = np.log10(off_events.energy)
    #
    # min_energy = min(on_events.energy.min(), off_events.energy.min())
    # max_energy = max(on_events.energy.max(), off_events.energy.max())
    #
    # bin_edges = np.linspace(min_energy, max_energy, 20)
    #
    # on_events['energy_bin'] = pd.cut(on_events.energy, bin_edges)
    # off_events['energy_bin'] = pd.cut(off_events.energy, bin_edges)
    #
    # n_on = on_events.groupby('energy_bin').sum()['weight']
    # n_off = off_events.groupby('energy_bin').sum()['weight']
    #
    # sens, err = relative_sensitivity(
    #     n_on, n_off, alpha=1, t_obs=t_obs, t_ref=t_obs)
    #
    # sens = sens * 1 / (u.TeV * u.s * u.m**2)
    #
    # sens = sens.to(1 / (u.erg * u.s * u.cm**2))

    #
    # protons = protons.query('gammaness >= {}'.format(gammaness))
    #
    # gammas = gammas.query('gammaness >= {}'.format(gammaness))
    #
    # _, edges, _ = plt.hist(protons.theta_deg**2,
    #                        bins=n_bins, range=[0, .15], histtype='step')
    # plt.hist(gammas.theta_deg**2,
    #          bins=edges, histtype='step')
    # plt.show()


if __name__ == '__main__':
    main()
