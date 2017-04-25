import numpy as np
import click
import pandas as pd
import matplotlib.pyplot as plt
import astropy.units as u
import power_law
import cta_io



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
    Plot the famous theta square curve.
    '''
    t_obs = 3.6 * u.h

    # read the gammas and weight them accroding to the crab spectrum
    gammas, N, e_min, e_max, area = cta_io.read_events(predicted_gammas, mc_production_information)
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

    # read the protons and weight them accroding to the cosmic ray spectrum
    protons, N, e_min, e_max, area = cta_io.read_events(predicted_protons, mc_production_information)
    cosmic_spectrum = power_law.CosmicRaySpectrum()
    energies = protons.energy.values*u.TeV

    protons['weight'] = cosmic_spectrum.weight(
        energies,
        e_min,
        e_max,
        area,
        t_assumed_obs=t_obs,
        simulated_showers=N
    )

    # select gamma-like events from both samples and plot the theta histogram
    selected_protons = protons.query('gammaness >= 0.8')
    selected_gammas = gammas.query('gammaness >= 0.8')

    _, edges, _ = plt.hist(
                    selected_protons.theta_deg**2,
                    bins=n_bins,
                    range=[0, 0.2],
                    weights=selected_protons.weight,
                    histtype='step',
                )
    plt.hist(
        selected_gammas.theta_deg**2,
        bins=edges,
        weights=selected_gammas.weight,
        histtype='step',
    )
    plt.xlabel('$(\mathrm{Theta} / \mathrm{degree})^2$')
    plt.ylabel('Expected events in {} '.format(t_obs))
    plt.savefig(outputfile)


if __name__ == '__main__':
    main()
