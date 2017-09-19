import numpy as np
import click
import astropy.units as u
import power_law
import cta_io
import matplotlib.pyplot as plt
from fact.analysis import li_ma_significance
from scipy.optimize import newton





@click.command()
@click.argument('predicted_gammas', type=click.Path(exists=True, dir_okay=False,))
@click.argument('predicted_protons', type=click.Path(exists=True, dir_okay=False,))
@click.argument('mc_production_information', type=click.Path(exists=True))
@click.argument('outputfile', type=click.Path(exists=False, dir_okay=False,))
@click.option('-n', '--n_bins', type=click.INT, default=4, help='energy bins to plot')
@click.option('-s', '--sample_fraction', type=click.FLOAT, default=0.5, help='MC sample fraction')
def main(
    predicted_gammas,
    predicted_protons,
    mc_production_information,
    outputfile,
    n_bins,
    sample_fraction,
):
    '''
    Plots a sensitivity curve vs real energy. For each energy bin it performs a gridsearch
    to find the theta and gammaness cuts that produce the highest sensitivity.
    '''

    t_obs = 60 * u.s
    alpha = 0.2
    signal_region_radius = 0.2
    off_region_radius = np.sqrt(1 / alpha) * signal_region_radius  # make are five times as large
    gamma_selection = 'gammaness > 0.65 & theta_deg < @signal_region_radius'
    proton_selection = 'gammaness > 0.65 & theta_deg < @off_region_radius'

    # read the protons and weight them accroding to the cosmic ray spectrum
    protons, N, e_min, e_max, area = cta_io.read_events(
        predicted_protons, mc_production_information)
    mc_proton_spectrum = power_law.MCSpectrum(
        e_min=e_min,
        e_max=e_max,
        total_showers_simulated=N,
        generation_area=area,
        generator_solid_angle=6 * u.deg
    )
    protons = protons.query(proton_selection)
    expected_background_events, edges = estimate_measured_events_per_bin(
        protons,
        mc_proton_spectrum,
        power_law.CosmicRaySpectrum(),
        t_obs=t_obs,
        bins=n_bins,
        sample_fraction=sample_fraction,
    )
    print('expected measured counts from cosmic rays')
    print(expected_background_events)
    #
    gammas, N, e_min, e_max, area = cta_io.read_events(
        predicted_gammas, mc_production_information)
    mc_gamma_spectrum = power_law.MCSpectrum(
        e_min=e_min,
        e_max=e_max,
        total_showers_simulated=N,
        generation_area=area,
    )
    gammas = gammas.query(gamma_selection)
    expected_signal_events, edges = estimate_measured_events_per_bin(
        gammas,
        mc_gamma_spectrum,
        power_law.CrabSpectrum(),
        t_obs=t_obs,
        bins=n_bins,
        sample_fraction=sample_fraction,
    )

    print('expected measured counts from crab')
    print(expected_signal_events)
    excess_counts = calculate_excess(expected_background_events, alpha=alpha)
    print(excess_counts)

    bin_center = 0.5 * (edges[:-1] + edges[1:])
    flux = excess_counts / expected_signal_events * power_law.CrabSpectrum().flux(bin_center)
    flux = flux.to('cm-2 erg-1 s-1')
    plot_sensitivity(bin_edges=edges, sensitivity=flux, t_obs=t_obs, scale=True)
    # import IPython; IPython.embed()
    plt.savefig(outputfile)



def estimate_measured_events_per_bin(events, mc_spectrum, target_spectrum, bins=30, t_obs=5 * u.h, sample_fraction=0.5):
        expected_mc_events, edges = mc_spectrum.expected_events_for_bins(
            bins=bins
        )
        expected_mc_events = expected_mc_events * sample_fraction
        efficiency = np.histogram(events.energy, bins=edges)[0] / expected_mc_events
        print('Efficiency for spectrum {}'.format(target_spectrum))
        print(efficiency)

        expected_physics_events, edges = target_spectrum.expected_events_for_bins(
            mc_spectrum.e_min,
            mc_spectrum.e_max,
            mc_spectrum.generation_area,
            t_obs=t_obs,
            solid_angle=mc_spectrum.generator_solid_angle,
            bins=bins
        )

        measured_events = efficiency * expected_physics_events
        return measured_events, edges


def calculate_excess(expected_background_counts, target_sigma=5, min_number_of_gammas=10, alpha=1):
    '''
    Find the number of needed gamma excess events using newtons method.
    Defines a function `significance_on_off(x, off, alpha) - self.sigma`
    and uses scipy.optimize.newton to find the `x` for which this function
    is zero.
    '''

    def target_function(on, off, alpha):
        return li_ma_significance(on, off, alpha=alpha) - target_sigma

    excess = np.zeros_like(expected_background_counts)
    for energy_bin, bg_count in enumerate(expected_background_counts):
        # if the number of bg events is to small just return the predefined minimum
        if bg_count / alpha < 1:
            excess[energy_bin] = min_number_of_gammas
            continue

        off = bg_count / alpha
        # provide a proper start guess for the minimizer
        on = bg_count + min_number_of_gammas
        e = newton(target_function, x0=on, args=(off, alpha))

        # excess is defined as the number of on events minues the number of background events
        excess[energy_bin] = e - bg_count

    return excess


def plot_sensitivity(bin_edges, sensitivity, t_obs, ax=None, scale=True, **kwargs):
    error = None

    if not ax:
        _, ax = plt.subplots(1)

    bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
    bin_width = np.diff(bin_edges)

    sensitivity = sensitivity.to(1 / (u.erg * u.s * u.cm**2))

    if sensitivity.ndim == 2:
        error = sensitivity.std(axis=0) / 2
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
        ax.set_ylabel(r'$ (E / \mathrm{erg} )^2 \cdot \mathrm{photons} /( \mathrm{erg} \quad \mathrm{s} \quad  \mathrm{cm}^2$ )  in ' + str(t_obs.to('h')) )
    ax.set_xlabel(r'$E /  \mathrm{TeV}$')

    return ax


if __name__ == '__main__':
    main()
