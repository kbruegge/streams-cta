import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
import power_law


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
    df = df[~df.source_file.str.contains('NG_cone10')]
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
    # t_obs = 50 * u.h
    # gammaness = 0.5

    df_mc = pd.read_csv(mc_production_information)

    gammas = read_events(predicted_gammas)

    crab = power_law.CrabSpectrum()
    N, e_min, e_max, area = get_mc_information(gammas, df_mc)

    gammas['weight'] = crab.weight(
                            gammas.energy.values * u.TeV,
                            e_min,
                            e_max,
                            area=area,
                            simulated_showers=2*N,
                            simulated_index=-2.0
                        )

    selected_gammas = gammas.query('gammaness >= 0.7')
    from IPython import embed; embed()
    events, edges = crab.expected_events_for_bins(
            e_min=e_min,
            e_max=e_max,
            area=area,
            t_obs=1*u.s,
            bins=20
        )

    bin_center = 0.5 * (edges[:-1] + edges[1:])
    bin_width = np.diff(edges)

    plt.errorbar(
            bin_center.value,
            events,
            xerr=bin_width.value*0.5,
            linestyle='',
            marker='.',
            label='expected events from crab',
            color='black',
        )
    plt.hist(
            gammas.energy,
            bins=edges,
            histtype='step',
            label='triggered gammas',
            color='gray',
        )

    plt.hist(
            gammas.energy,
            bins=edges,
            histtype='step',
            label='triggered and reweighted gammas',
            weights=gammas.weight,
        )
    plt.hist(
            selected_gammas.energy,
            bins=edges,
            histtype='step',
            label='selected and reweighted gammas',
            weights=selected_gammas.weight,
        )
    plt.yscale('log')
    plt.xscale('log')
    plt.xlabel('Energy in TeV')
    plt.legend()
    plt.show()


if __name__ == '__main__':
    main()
