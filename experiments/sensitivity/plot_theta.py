import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import uncertainties as unc
import uncertainties.unumpy as unp
import astropy.units as u
import power_law
from coordinates import distance_between_estimated_and_mc_direction
from astropy.coordinates import cartesian_to_spherical, Angle, EarthLocation, SkyCoord, Latitude, Longitude
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
        on_events,
        off_events,
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
        signal-like events for the on observations
    n_off: int or array-like
        signal-like events for the off observations
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

    on_events.energy = np.log10(on_events.energy)
    off_events.energy = np.log10(off_events.energy)

    min_energy = min(on_events.energy.min(), off_events.energy.min())
    max_energy = max(on_events.energy.max(), off_events.energy.max())

    bin_edges = np.linspace(min_energy, max_energy, 20)

    on_events['energy_bin'] = pd.cut(on_events.energy, bin_edges)
    off_events['energy_bin'] = pd.cut(off_events.energy, bin_edges)

    n_on = on_events.groupby('energy_bin')['weight'].sum()
    n_off = off_events.groupby('energy_bin')['weight'].sum()

    ratio = (t_obs / t_ref).decompose().value

    err = np.sqrt(on_events.groupby('energy_bin')['weight'].count().values)
    n_on = unp.uarray(n_on, err)

    err = np.sqrt(off_events.groupby('energy_bin')['weight'].count().values)
    n_off = unp.uarray(n_off, err)

    t_off = n_off * unp.log(n_off * (1 + alpha) / (n_on + n_off))
    t_on = n_on * unp.log(n_on * (1 + alpha) / alpha / (n_on + n_off))

    sensitivity = significance**2 / 2 * ratio / (t_on + t_off)

    return unp.nominal_values(sensitivity), unp.std_devs(sensitivity), bin_edges


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
    lat = Latitude((24, 37, 38), unit='deg')
    lon = Longitude((70, 34, 15), unit='deg')
    paranal = EarthLocation.from_geodetic(lon, lat, 2600)
    # paranal = EarthLocation.of_site('paranal')
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


def calc_e_0(index, simulated_index, e_min, e_max):
    e_0 = (1 + index)/(1 + simulated_index) * \
            ((e_max**(1 + simulated_index)) - (e_min**(1 + simulated_index))) \
                / ((e_max**(1 + index)) - (e_min**(1 + index)))

    e_0 = e_0**(1/(simulated_index - index))
    return e_0

def weigh_protons(events, mc_information, t_obs):
    N, e_min, e_max, area = get_mc_information(events, mc_information)
    index = -2.70
    simulated_index = -2.0
    solidangle = 2 * np.pi * (1 - np.cos(np.deg2rad(6))) * u.sr
    norm = 9.6E-9 * (1/(u.GeV * u.s * u.sr * u.cm**2)) * solidangle
    e_norm = 1000 * u.GeV

    e_0 = calc_e_0(index, simulated_index, e_min.to('GeV').value, e_max.to('GeV').value) * u.GeV

    flux = norm/(1 + index) * e_norm**(-index) * ( (e_max**(1 + index)) - (e_min**(1 + index)) )

    flux = flux * area * t_obs

    energy = events.energy.values * u.TeV
    weight = (energy/e_0)**(index - simulated_index) * flux / N
    assert weight.decompose().unit.is_unity() is True

    return weight

def weigh_gammas(events, mc_information, t_obs):
    N, e_min, e_max, area = get_mc_information(events, mc_information)
    index = -2.62
    simulated_index = -2.0
    norm = 2.83E-14 * (1/(u.GeV * u.s * u.cm**2))
    e_norm = 1000 * u.GeV

    e_0 = calc_e_0(index, simulated_index, e_min.to('GeV').value, e_max.to('GeV').value) * u.GeV

    flux = norm/(1 + index) * e_norm**(-index) * ( (e_max**(1 + index)) - (e_min**(1 + index)) )

    flux = flux * area * t_obs

    energy = events.energy.values * u.TeV
    weight = (energy/e_0)**(index - simulated_index) * flux / N
    assert weight.decompose().unit.is_unity() is True

    return weight


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
    t_obs = 50 * u.h
    # gammaness = 0.5

    df_mc = pd.read_csv(mc_production_information)

    gammas = read_events(predicted_gammas)
    protons = read_events(predicted_protons)

    crab = power_law.CrabSpectrum()
    N, e_min, e_max, area = get_mc_information(gammas, df_mc)
    energies = gammas.energy.values*u.GeV
    gammas['weight'] = crab.event_weights(energies, N, e_min, e_max, area,)

    cosmic_spectrum = power_law.CosmicRaySpectrum()
    N, e_min, e_max, area = get_mc_information(protons, df_mc)
    energies = protons.energy.values*u.GeV
    protons['weight'] = cosmic_spectrum.event_weights(energies, N, e_min, e_max, area,)

    # weigh_events(gammas, df_mc, t_obs, spectrum=power_law.CrabSpectrum())
    # weigh_events(protons, df_mc, t_obs, spectrum=power_law.CosmicRaySpectrum())

    selected_protons = protons.query('gammaness >= 0.7')
    selected_gammas = gammas.query('gammaness >= 0.7')

    inner_radius = 0.014
    outter_radius = 0.1

    on_events = selected_gammas.query('theta_deg_squared < {}'.format(inner_radius)).copy()
    off_events = selected_protons.query('theta_deg_squared < {}'.format(outter_radius)).copy()

    # calculate ratio of areas
    area_on = np.pi * inner_radius**2
    area_off = np.pi * outter_radius**2
    alpha = (area_on / area_off)

    sens, err, bin_edges = relative_sensitivity(
        on_events, off_events, alpha=alpha, t_obs=t_obs, t_ref=50*u.h,
    )

    sens = sens * 1 / (u.TeV * u.s * u.m**2)
    err = err * 1 / (u.TeV * u.s * u.m**2)

    # sens = sens.to(1 / (u.erg * u.s * u.cm**2))


    from IPython import embed
    embed()

    event_energies = np.log10(gammas.energy)
    H, _, _ = plt.hist(event_energies, bins=bin_edges, weights=gammas.weight, label='Triggered Events')
    plt.yscale('log')


    events = []
    N, e_min, e_max, area = get_mc_information(gammas, df_mc)
    for e_low, e_high in zip(bin_edges[0:], bin_edges[1:]):
        e = crab.expected_events(e_low*u.TeV, e_high, area=area, t_obs=t_obs)
        events.append(e)

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
