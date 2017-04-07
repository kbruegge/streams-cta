import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import uncertainties as unc
import uncertainties.unumpy as unp
import astropy.units as u
import power_law
from coordinates import distance_between_estimated_and_mc_direction
from astropy.coordinates import cartesian_to_spherical, Angle, EarthLocation, SkyCoord
from dateutil import parser
from scipy.integrate import quad




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




def estimated_alt_az(df):
    x = df['stereo:estimated_direction:x']
    y = df['stereo:estimated_direction:y']
    z = df['stereo:estimated_direction:z']

    r, lat, lon = cartesian_to_spherical(
        x.values * u.m, y.values * u.m, z.values * u.m)

    alt = Angle(90 * u.deg - lat).to('deg')
    az = Angle(lon).wrap_at(180 * u.deg).to('deg')

    return alt, az


def distance_to_source(alt, az, source_alt, source_az):

    paranal = EarthLocation.of_site('paranal')
    dt = parser.parse('2017-09-20 22:15')

    c = SkyCoord(
        alt=alt,
        az=az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    c_source = SkyCoord(
        alt=source_alt,
        az=source_az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    return c.separation(c_source)


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

    alt, az = estimated_alt_az(df)

    source_alt = Angle(90 * u.deg - source_alt * u.deg).to('deg')
    source_az = Angle(source_az * u.deg).to('deg')

    df['theta_deg'] = distance_to_source(alt, az, source_alt, source_az)
    df['theta_deg_squared'] = df['theta_deg']**2
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


def weigh_events(
    events,
    mc_information,
    t_obs,
    index_mc=-2.0,
    spectrum=power_law.CrabSpectrum(),
):

    N, e_min, e_max, area = get_mc_information(events, mc_information)

    expected_events = spectrum.expected_events(e_min, e_max, area, t_obs)

    energy = events.energy.values * u.TeV
    events['weight'] = spectrum.event_weights(
        energy,
        N,
        expected_events,
        index_mc=index_mc,
    )


@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_bins', type=click.INT, default=30, help='theta bin')
def main(
    predicted_gammas,
    predicted_protons,
    mc_production_information,
    outputfile,
    n_bins,
):
    '''
    Plot the sensitivity curve.
    '''
    t_obs = 3.6 * u.h
    gammaness = 0.5

    df_mc = pd.read_csv(mc_production_information)

    gammas = read_events(predicted_gammas)
    protons = read_events(predicted_protons)

    weigh_events(gammas, df_mc, t_obs, spectrum=power_law.CrabSpectrum())
    weigh_events(protons, df_mc, t_obs, spectrum=power_law.CosmicRaySpectrum())

    selected_protons = protons.query('gammaness >= 0.5')
    selected_gammas = gammas.query('gammaness >= 0.5')

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

    from IPython import embed
    embed()

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
