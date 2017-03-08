import click
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import astropy.units as u
from astropy.coordinates import cartesian_to_spherical, Angle
from matplotlib.colors import LogNorm
plt.style.use('ggplot')


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    df = pd.read_csv(input_file).dropna()

    x = df['stereo:estimated_direction:x']
    y = df['stereo:estimated_direction:y']
    z = df['stereo:estimated_direction:z']

    r, lat, lon = cartesian_to_spherical(x.values*u.m, y.values*u.m, z.values*u.m)

    alt = Angle(90*u.deg - lat).degree
    mc_alt = Angle(df['mc:alt'].values*u.rad).degree

    az = Angle(lon).wrap_at(180*u.deg).degree
    mc_az = Angle(df['mc:az'].values*u.rad).wrap_at(180*u.deg).degree

    resolution = np.sqrt(
                np.percentile(alt - mc_alt, 68)**2 + np.percentile(az - mc_az, 68)**2
            )

    plt.hist2d(
     alt, az, range=[[69.5, 70.5], [-0.4, 0.4]], bins=100,  cmap='viridis', norm=LogNorm()
    )

    plt.plot(np.unique(mc_alt), np.unique(mc_az), '+', ms=20, mew=2, color='red')
    plt.text(69.55, -0.37, 'Angular Resolution ~ {:.2f}'.format(resolution))
    plt.colorbar()

    # import IPython; IPython.embed()
    # plt.hist(alt, bins=bins, range=[-0.02, 0.02])
    # plt.show()
    # bins = 100
    # fig, (ax1, ax2) = plt.subplots(1, 2)
    # ax1.hist(alt, bins=bins, range=[69, 71], histtype='step', lw=2)
    # ax1.hist(mc_alt, bins=bins, range=[69, 71], histtype='step', lw=1)
    # ax1.set_ylim([0, 5000])
    # # ax1.legend()
    #
    # ax2.hist(az, bins=bins, range=[-0.3, 0.3], histtype='step', lw=2)
    # ax2.hist(mc_az, bins=bins, range=[-0.3, 0.3], histtype='step', lw=1)
    # ax2.set_ylim([0, 5000])
    # # ax2.legend()

    # plt.show()
    plt.savefig(output_file)

if __name__ == "__main__":
    main()
