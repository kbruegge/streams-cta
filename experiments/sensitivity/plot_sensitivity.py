import numpy as np
import click
import matplotlib.pyplot as plt
from tqdm import tqdm
import astropy.units as u
import power_law
import cta_io
import pandas as pd


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
        error = error * bin_center.to('erg')**2

    ax.errorbar(
        bin_center.value,
        sensitivity.value,
        xerr=bin_width.value * 0.5,
        yerr=error.value,
        marker='.',
        linestyle='',
        capsize=0,
        **kwargs,
    )

    ax.set_yscale('log')
    ax.set_xscale('log')
    ax.set_ylabel(r'$ \mathrm{photons} / \mathrm{erg s} \mathrm{cm}^2$ in ' + str(t_obs.to('h')) + ' hours' )
    if scale:
        ax.set_ylabel(r'$ E^2 \cdot \mathrm{photons} / \mathrm{erg} \quad \mathrm{s} \quad  \mathrm{cm}^2$ in ' + str(t_obs.to('h')) )
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

    # ax.set_yscale('log')
    # ax.set_xscale('log')
    # ax.set_ylabel(r'$Area / \mathrm{m}^2$')
    # ax.set_xlabel(r'$\log_{10}(E /  \mathrm{TeV})$')

    return ax


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

    # select gamma-like events from both samples and plot the theta histogram
    selected_protons = protons.query('gammaness >= 0.8').copy()
    selected_gammas = gammas.query('gammaness >= 0.8').copy()

    min_energy = min(selected_gammas.energy.min(),
                     selected_protons.energy.min())
    max_energy = max(selected_gammas.energy.max(),
                     selected_protons.energy.max())

    edges = np.logspace(np.log10(min_energy), np.log10(
        max_energy), num=n_bins + 1, base=10.0) * u.TeV

    selected_gammas['energy_bin'] = pd.cut(selected_gammas.energy, edges)
    selected_protons['energy_bin'] = pd.cut(selected_protons.energy, edges)

    background_region_radius = 0.01
    signal_region_radius = 0.01

    # to estimate the number of off events take the mean over the a larger
    # theta area.

    sensitivity = []
    bin_center = 0.5 * (edges[:-1] + edges[1:])
    for _ in tqdm(range(iterations)):
        # s = np.random.poisson(n_on_unweighted)
        # b = np.random.poisson(n_off_unweighted)

        n_off = []
        for _, group in selected_protons.groupby('energy_bin'):
            b = np.random.poisson(len(group))

            if b == 0:
                n_off.append(0)
                continue

            sample = group.sample(b, replace=True)

            H, _ = np.histogram(
                sample.theta_deg**2,
                bins=np.arange(0, 0.2, background_region_radius),
                weights=sample.weight
            )
            n_off.append(H.mean())

        n_off = np.array(n_off)

        n_on = []
        for _, group in selected_gammas.groupby('energy_bin'):
            b = np.random.poisson(len(group))
            if b == 0:
                n_on.append(0)
                continue

            on = group.query(
                    'theta_deg_squared < {}'.format(signal_region_radius)
                )\
                .sample(b, replace=True)['weight']\
                .sum()

            n_on.append(on)

        n_on = np.array(n_on)
    #
        relative_flux = power_law.relative_sensitivity(
            n_on, n_off,
            alpha=signal_region_radius/background_region_radius,
        )
        sens = crab.flux(bin_center) * relative_flux
        sensitivity.append(sens)

    sensitivity = np.array(sensitivity) * sensitivity[0][0].unit

    # from IPython import embed; embed()
    # print('relative flux {}'.format(relative_flux))
    # bin_center = 0.5 * (edges[:-1] + edges[1:])
    # sens = crab.flux(bin_center) * relative_flux
    # sens = sens.to(1 / (u.erg * u.s * u.cm**2))
    # print('Sens {}'.format(sens))
    ax = plot_sensitivity(edges, sensitivity, t_obs, ax=None)
    plot_spectrum(crab, e_min, e_max, ax=ax, color='gray')
    plt.savefig(outputfile)
    # on_events.energy = np.log10(on_events.energy)
    # off_events.energy = np.log10(off_events.energy)
    # print(n_off, n_on)

    # event_energies = np.log10(selected_gammas.energy)
    # a = e_min.to('TeV').value
    # b = e_max.to('TeV').value
    # edges = np.logspace(np.log10(a), np.log10(b), num=bins, base=10.0) * u.TeV
    # bin_edges = np.linspace(e_min, e_max, num=5)
    # H, _, _ = plt.hist(event_energies, bins=bin_edges, weights=selected_gammas.weight, label='Triggered Events')
    # plt.yscale('log')
    # from IPython import embed; embed()

    # plt.show()


if __name__ == '__main__':
    main()
