import numpy as np
import click
import matplotlib.pyplot as plt
import astropy.units as u
import power_law
import cta_io


@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_bins', type=click.INT, default=30, help='theta bin')
def main(
        predicted_gammas,
        mc_production_information,
        outputfile,
        n_bins,
):
    '''
    Plot the efficency for scaled events.
    '''
    t_obs = 50 * u.h

    gammas, N, e_min, e_max, area = cta_io.read_events(
                predicted_gammas,
                mc_production_information
            )

    crab = power_law.CrabSpectrum()
    energies = gammas.energy.values*u.TeV

    gammas['weight'] = crab.weight(
        energies,
        e_min,
        e_max,
        area,
        t_assumed_obs=t_obs,
        simulated_showers=N
    )

    selected_gammas = gammas.query('gammaness >= 0.8')
    # from IPython import embed; embed()
    events, edges = crab.expected_events_for_bins(
            e_min=e_min,
            e_max=e_max,
            area=area,
            t_obs=t_obs,
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
    plt.xlabel('Energy / TeV')
    plt.ylabel('Expected Events')
    plt.legend()
    plt.savefig(outputfile)


if __name__ == '__main__':
    main()
