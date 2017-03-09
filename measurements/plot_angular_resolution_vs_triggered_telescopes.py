import click
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import astropy.units as u
from astropy.coordinates import cartesian_to_spherical, Angle, SkyCoord, EarthLocation
from dateutil import parser
import seaborn as sns
from os.path import splitext


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    df = pd.read_csv(input_file).dropna()

    x = df['stereo:estimated_direction:x']
    y = df['stereo:estimated_direction:y']
    z = df['stereo:estimated_direction:z']

    r, lat, lon = cartesian_to_spherical(x.values*u.m, y.values*u.m, z.values*u.m)

    alt = Angle(90*u.deg - lat)
    mc_alt = Angle(df['mc:alt'].values*u.rad)

    az = Angle(lon).wrap_at(180*u.deg)
    mc_az = Angle(df['mc:az'].values*u.rad).wrap_at(180*u.deg)

    # paranal = EarthLocation.of_site('paranal')
    paranal = EarthLocation(1946618*u.m, -5467645*u.m, -2642488*u.m)
    dt = parser.parse('1987-09-20 22:15')

    c = SkyCoord(
            alt=alt,
            az=az,
            obstime=dt,
            frame='altaz',
            location=paranal,
            )

    c_mc = SkyCoord(
            alt=mc_alt,
            az=mc_az,
            obstime=dt,
            frame='altaz',
            location=paranal,
            )

    df['spherical_distance'] = c.separation(c_mc)
    df['eucledian_distance'] = np.sqrt(
                (alt - mc_alt)**2 + (az - mc_az)**2
            )

    df['manhattan_distance'] = np.abs(alt - mc_alt) + np.abs(az - mc_az)

    # import IPython; IPython.embed()

    # whis=[15.7, 15.7+68.2]
    sns.boxplot(x='array:num_triggered_telescopes', y='spherical_distance', data=df, fliersize=1, linewidth=1)
    plt.ylabel('Great Circle Distance')
    plt.ylim([-1, 1])
    plt.savefig(output_file)

    plt.figure()
    sns.boxplot(x='array:num_triggered_telescopes', y='eucledian_distance', data=df, fliersize=1, linewidth=1)
    plt.ylabel('Eucledian Distance')
    plt.ylim([-1, 1])
    name, extension = splitext(output_file)
    plt.savefig(name + '_eucledian' + extension)

    plt.figure()
    sns.boxplot(x='array:num_triggered_telescopes', y='manhattan_distance', data=df, fliersize=1, linewidth=1)
    plt.ylabel('Mahattan Distance')
    plt.ylim([-1, 1])
    name, extension = splitext(output_file)
    plt.savefig(name + '_manhattan' + extension)

if __name__ == "__main__":
    main()
