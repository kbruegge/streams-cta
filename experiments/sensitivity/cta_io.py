from astropy.coordinates import Angle, cartesian_to_spherical
import astropy.units as u
import pandas as pd
import numpy as np
import coordinates


def _estimated_alt_az(df):
    x = df['stereo:estimated_direction:x']
    y = df['stereo:estimated_direction:y']
    z = df['stereo:estimated_direction:z']

    r, lat, lon = cartesian_to_spherical(
        x.values * u.m, y.values * u.m, z.values * u.m)

    alt = Angle(90 * u.deg - lat).to('deg')
    az = Angle(lon).wrap_at(180 * u.deg).to('deg')

    return alt, az


def read_events(path_to_data_file, path_to_mc_file, source_alt=20, source_az=0):
    '''
    Read events and the corresponding monte carlo meta data from a csv file.
    given the path to the event data and the monte carlo production information
    this method returns a dataframe containing the eventwise information, including
    theta and theta_square, and information about the simulation i.e
        n_simulated_showers, e_min, e_max, generator_area

    '''
    df = pd.read_csv(path_to_data_file).dropna()
    df.rename(index=str,
              columns={
                  'prediction:signal:mean': 'gammaness',
                  'mc:energy': 'energy'
              },
              inplace=True,
              )

    df = df[df.source_file.str.contains('_20deg_0deg')]

    alt, az = _estimated_alt_az(df)

    source_alt = Angle(90 * u.deg - source_alt * u.deg).to('deg')
    source_az = Angle(source_az * u.deg).to('deg')

    df['theta_deg'] = coordinates.distance_to_source(alt, az, source_alt, source_az)
    df['theta_deg_squared'] = df['theta_deg']**2

    df_mc = pd.read_csv(path_to_mc_file)
    n_simulated_showers, e_min, e_max, generator_area = _get_mc_information(df, df_mc)
    return df, n_simulated_showers, e_min, e_max, generator_area


def _get_mc_information(df, df_mc):
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
