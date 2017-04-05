import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import uncertainties as unc
import uncertainties.unumpy as unp
import astropy.units as u
import power_law
from scipy.integrate import quad
from coordinates import distance_between_estimated_and_mc_direction


def plot_sensitivity(bin_edges, sensitivity, ax=None, error=None, **kwargs):

    if not ax:
        _, ax = plt.subplots(1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    ax.errorbar(
        bin_center,
        sensitivity,
        xerr=bin_width * 0.5,
        yerr=error,
        marker='.',
        linestyle='',
        capsize=0,
        **kwargs,
    )

    ax.set_yscale('log')
    # ax.set_ylabel(r'$Area / \mathrm{m}^2$')
    ax.set_xlabel(r'$\log_{10}(E /  \mathrm{TeV})$')

    return ax


@u.quantity_input(t_obs=u.hour, t_ref=u.hour)
def relative_sensitivity(
        n_on,
        n_off,
        alpha,
        t_obs,
        t_ref=50 * u.hour,
        significance=5,
):
    '''
    Calculate the relative sensitivity defined as the flux
    relative to the reference source that is detectable with
    significance in t_ref.

    Parameters
    ----------
    n_on: int or array-like
        Number of signal-like events for the on observations
    n_off: int or array-like
        Number of signal-like events for the off observations
    alpha: float
        Scaling factor between on and off observations.
        1 / number of off regions for wobble observations.
    t_obs: astropy.units.Quantity of type time
        Total observation time
    t_ref: astropy.units.Quantity of type time
        Reference time for the detection
    significance: float
        Significance necessary for a detection
    '''
    ratio = (t_obs / t_ref).decompose().value

    n_on = unp.uarray(n_on, np.sqrt(n_on))
    n_off = unp.uarray(n_off, np.sqrt(n_off))

    t_off = n_off * unp.log(n_off * (1 + alpha) / (n_on + n_off))
    t_on = n_on * unp.log(n_on * (1 + alpha) / alpha / (n_on + n_off))

    sensitivity = significance**2 / 2 * ratio / (t_on + t_off)

    return unp.nominal_values(sensitivity), unp.std_devs(sensitivity)


def distance_from_source(df):
    x = df['stereo:estimated_direction:x'].values * u.m
    y = df['stereo:estimated_direction:y'].values * u.m
    z = df['stereo:estimated_direction:z'].values * u.m

    mc_alt = df['mc:alt'].values * u.rad
    mc_az = df['mc:az'].values * u.rad

    theta = distance_between_estimated_and_mc_direction(
        x, y, z, mc_alt, mc_az).to('deg')
    return theta


def read_and_select_events(
    gamma_path,
    proton_path,
    gammaness=0.5,
    inner_radius=0.1 * u.deg,
    outter_radius=1 * u.deg,
    t_obs=5 * u.h,
):

    gammas = pd.read_csv(gamma_path)
    gammas['true_label'] = 1

    protons = pd.read_csv(proton_path)
    protons['true_label'] = 0

    df = pd.concat([gammas, protons])

    df['theta_deg'] = distance_from_source(df)
    df.rename(index=str,
              columns={
                  'prediction:signal:mean': 'gammaness',
                  'mc:energy': 'energy'
              },
              inplace=True,
              )

    # select gamma-like events
    df = df.query('gammaness >= {}'.format(gammaness))

    # select background and source events
    df = df.query(
        'theta_deg <= {}'.format(inner_radius.value)
    )

    return df.query('true_label == 1'), df.query('true_label == 0')


def get_mc_information(path_to_events, path_to_mc_information):
    # read montecarlo production meta data
    mc = pd.read_csv(path_to_mc_information)
    events = pd.read_csv(path_to_events)

    if len(events.source_file.unique()) > len(mc.file_names):
        print('Shits going down yo')

    mcs_for_events = mc.query(
        'file_names in @events.source_file.unique()'
    )

    n_simulated_showers = mcs_for_events.simulated_showers.sum()

    e_min = mcs_for_events.energy_min.min() * u.TeV
    e_max = mcs_for_events.energy_max.max() * u.TeV

    if len(mcs_for_events.energy_min.unique()) > 1:
        print('different enregie ranges simulated? screw dat!')

    if len(mcs_for_events.scatter_radius_meter.unique()) > 1:
        print('different scatter radii simulated? screw dat!')

    area = np.pi * mcs_for_events.scatter_radius_meter.min()**2 * u.m**2

    # todo. add this to the file_names
    time = len(mcs_for_events) * u.second
    return n_simulated_showers, e_min, e_max, area, time



@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_energy', type=click.INT, default=11, help='energy bins')
def main(
    predicted_gammas,
    predicted_protons,
    mc_production_information,
    outputfile,
    n_energy,
):
    '''
    Plot the sensitivity curve.
    '''
    t_obs = 50 * u.hour

    on_events, off_events = read_and_select_events(
        predicted_gammas,
        predicted_protons,
        t_obs=t_obs,
    )

    N_gammas, e_min, e_max, area, simulation_time = get_mc_information(
        predicted_gammas, mc_production_information)

    index_crab = -2.48
    index_mc = -2.0

    def f(e, area, t_obs):
        return (power_law.crab_source_rate(e*u.TeV) * area.to('m^2') * t_obs.to('s')).value

    def weight(energy, simulated_showers, expected_showers, index_mc, index_target):
        w = (energy/u.TeV)**(index_mc - index_target) * expected_showers / simulated_showers
        return w

    expected_crab_events, _ = quad(lambda e: f(e, area, t_obs), e_min.value, e_max.value)

    energy = on_events.energy.values * u.TeV
    on_events['weight'] = weight(energy, N_gammas, expected_crab_events, index_mc, index_crab)

    # hmm
    # lecker
    N_protons, e_min, e_max, area, simulation_time = get_mc_information(
        predicted_protons, mc_production_information)

    index_cosmic = -3.0/8.0
    index_mc = -2.0

    def f_cosmic(e, area, t_obs):
        return (power_law.CR_background_rate(e*u.TeV) * area.to('m^2') * t_obs.to('s')).value

    expected_CR_events, _ = quad(lambda e: f_cosmic(e, area, t_obs), e_min.value, e_max.value)

    energy = off_events.energy.values * u.TeV
    off_events['weight'] = weight(energy, N_protons, expected_CR_events, index_mc, index_cosmic)


    on_events.energy = np.log10(on_events.energy)
    off_events.energy = np.log10(off_events.energy)

    min_energy = min(on_events.energy.min(), off_events.energy.min())
    max_energy = max(on_events.energy.max(), off_events.energy.max())

    bin_edges = np.linspace(min_energy, max_energy, 20)

    on_events['energy_bin'] = pd.cut(on_events.energy, bin_edges)
    off_events['energy_bin'] = pd.cut(off_events.energy, bin_edges)

    n_on = on_events.groupby('energy_bin').sum()['weight']
    n_off = off_events.groupby('energy_bin').sum()['weight']

    sens, err = relative_sensitivity(
        n_on, n_off, alpha=1, t_obs=t_obs, t_ref=t_obs)

    sens = sens * 1 / (u.TeV * u.s * u.m**2)

    sens = sens.to(1 / (u.erg * u.s * u.cm**2))


    from IPython import embed
    embed()


    fig, ax = plt.subplots(1)
    plot_sensitivity(bin_edges, sens.value, error=err, ax=ax)

    plt.show()


if __name__ == '__main__':
    main()
