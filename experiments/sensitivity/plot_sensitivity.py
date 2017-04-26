import numpy as np
import click
import matplotlib.pyplot as plt
from tqdm import tqdm
import astropy.units as u
import power_law
import cta_io
import pandas as pd
from scipy import optimize


@u.quantity_input(bin_edges=u.TeV, t_obs=u.h)
def plot_sensitivity(bin_edges, sensitivity, t_obs, ax=None, scale=True,  **kwargs):
    error = None

    if not ax:
        _, ax = plt.subplots(1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    sensitivity = sensitivity.to(1 / (u.erg * u.s * u.cm**2))

    if sensitivity.ndim == 2:
        error = sensitivity.std(axis=0)/2
        sensitivity = sensitivity.mean(axis=0)

    if scale:
        sensitivity = sensitivity * bin_center.to('erg')**2
        if error:
            error = error * bin_center.to('erg')**2

    ax.errorbar(
        bin_center.value,
        sensitivity.value,
        xerr=bin_width.value * 0.5,
        yerr=error.value if error else None,
        marker='.',
        linestyle='',
        capsize=0,
        **kwargs,
    )

    ax.set_yscale('log')
    ax.set_xscale('log')
    ax.set_ylabel(r'$ \mathrm{photons} / \mathrm{erg s} \mathrm{cm}^2$ in ' + str(t_obs.to('h')) + ' hours' )
    if scale:
        ax.set_ylabel(r'$ E^2 \cdot \mathrm{photons} /( \mathrm{erg} \quad \mathrm{s} \quad  \mathrm{cm}^2$ )  in ' + str(t_obs.to('h')) )
    ax.set_xlabel(r'$E /  \mathrm{TeV}$')

    return ax


@u.quantity_input(e_min=u.TeV, e_max=u.TeV)
def plot_spectrum(spectrum, e_min, e_max,  ax=None, scale=True, **kwargs):

    if not ax:
        _, ax = plt.subplots(1)

    e = np.linspace(e_min, e_max, 1000)
    flux = spectrum.flux(e).to(1 / (u.erg * u.s * u.cm**2))

    if scale:
        flux = flux*e.to('erg')**2

    ax.plot(
        e,
        flux,
        linestyle='--',
        **kwargs,
    )
    return ax


def calculate_sensitivity(
            gammas,
            protons,
            gammaness=0.5,
            signal_region=0.01,
            target_spectrum=power_law.CrabSpectrum()
        ):
    selected_gammas = gammas.query('gammaness >={}'.format(gammaness)).copy()

    selected_protons = protons.query('gammaness >={}'.format(gammaness)).copy()

    n_on, n_off = get_on_and_off_counts(
            selected_protons,
            selected_gammas,
            signal_region_radius=signal_region
        )

    relative_flux = power_law.relative_sensitivity(
            n_on,
            n_off,
            alpha=1,
        )

    min_energy = min(gammas.energy.min(),
                     protons.energy.min())
    max_energy = max(gammas.energy.max(),
                     protons.energy.max())

    bin_center = np.mean([min_energy, max_energy]) * u.TeV

    sens = target_spectrum.flux(bin_center) * relative_flux
    return sens


def get_on_and_off_counts(selected_protons, selected_gammas, signal_region_radius):
    """ Get on and off counts from the signal region using a simepl theta**2 cut"""

    # estimate n_off by assuming that the background rate is constant within a
    # smallish theta area around 0. take the mean of the thata square histogram
    # to get a more stable estimate for n_off
    background_region_radius = signal_region_radius
    # # print(selected_protons.theta_deg.count(), selected_protons.weight.count())
    # if selected_protons.weight.count() == 30:
    #     print(selected_protons.weight)
    #     from IPython import embed; embed()
    H, _ = np.histogram(
        selected_protons.theta_deg**2,
        bins=np.arange(0, 0.2, background_region_radius),
        weights=selected_protons.weight
    )
    n_off = H.mean()

    n_on = selected_gammas.query(
            'theta_deg_squared < {}'.format(signal_region_radius)
        )['weight']\
        .sum()

    return n_on, n_off


def create_sensitivity_matrix(
            protons,
            gammas,
            n_bins,
            iterations,
            target_spectrum=power_law.CrabSpectrum(),
        ):

    min_energy = min(gammas.energy.min(),
                     protons.energy.min())
    max_energy = max(gammas.energy.max(),
                     protons.energy.max())

    edges = np.logspace(np.log10(min_energy), np.log10(
        max_energy), num=n_bins + 1, base=10.0) * u.TeV

    gammas['energy_bin'] = pd.cut(gammas.energy, edges)
    protons['energy_bin'] = pd.cut(protons.energy, edges)

    gammaness_cuts = []
    theta_square_cuts = []
    sensitivity = []

    unit = None

    for b in tqdm(gammas.energy_bin.cat.categories):
        g = gammas[gammas.energy_bin == b]
        p = protons[protons.energy_bin == b]

        # print('category: {} len g {} len p {}'.format(b, len(g), len(p)))

        def f(x):
            return calculate_sensitivity(g, p, gammaness=x[0], signal_region=x[1]).value

        ranges = (slice(0.5, 1, 0.1), slice(0.005, 0.025, 0.005))
        # note: while it seems obviuous to use finish=optimize.fmin here. apparently it
        # tests invalid values. and then everything brakes
        res = optimize.brute(f, ranges, finish=None,  full_output=True)

        cuts = res[0]
        gammaness_cuts.append(cuts[0])
        theta_square_cuts.append(cuts[1])
        sens = calculate_sensitivity(g, p, gammaness=cuts[0], signal_region=cuts[1])
        # print(cuts, sens)
        sensitivity.append(sens.value)
        unit = sens.unit

    # multiply the whole thing by the proper unit. There must be a nicer way to do this.
    sensitivity = np.array(sensitivity) * unit
    return sensitivity, edges


@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_bins', type=click.INT, default=4, help='theta bin')
@click.option('-i', '--iterations', type=click.INT, default=100, help='iterations')
def main(
    predicted_gammas,
    predicted_protons,
    mc_production_information,
    outputfile,
    n_bins,
    iterations,
):
    '''
    Plot the sensitivity curve.
    '''
    t_obs = 50 * u.h

    # read the gammas and weight them accroding to the crab spectrum
    gammas, N, e_min, e_max, area = cta_io.read_events(
        predicted_gammas, mc_production_information)
    crab = power_law.CrabSpectrum()
    energies = gammas.energy.values * u.TeV

    gammas['weight'] = crab.weight(
        energies,
        e_min,
        e_max,
        area,
        t_assumed_obs=t_obs,
        simulated_showers=N
    )

    # read the protons and weight them accroding to the cosmic ray spectrum
    protons, N, e_min, e_max, area = cta_io.read_events(
        predicted_protons, mc_production_information)
    cosmic_spectrum = power_law.CosmicRaySpectrum()
    energies = protons.energy.values * u.TeV

    protons['weight'] = cosmic_spectrum.weight(
        energies,
        e_min,
        e_max,
        area,
        t_assumed_obs=t_obs,
        simulated_showers=N
    )

    sens, edges = create_sensitivity_matrix(protons, gammas, n_bins, iterations)
    sens = sens.to(1 / (u.erg * u.s * u.cm**2))
    ax = plot_sensitivity(edges, sens, t_obs, ax=None)
    plot_spectrum(crab, e_min, e_max, ax=ax, color='gray')
    plt.savefig(outputfile)
if __name__ == '__main__':
    main()
