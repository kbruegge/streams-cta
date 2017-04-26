import numpy as np
import astropy.units as u
from scipy.optimize import minimize_scalar
from fact.analysis import li_ma_significance


@u.quantity_input(t_obs=u.hour, t_ref=u.hour)
def relative_sensitivity(
        n_on,
        n_off,
        alpha,
        target_significance=5,
        ):
    '''
    Calculate the relative sensitivity defined as the flux
    relative to the reference source that is detectable with
    significance in t_ref.

    Parameters
    ----------
    n_on: int or array-like
        Number of signal-like events for the on observations
    n_off: int or array-like
        Number of signal-like events for the off observations
    alpha: float
        Scaling factor between on and off observations.
        1 / number of off regions for wobble observations.
    target_significance: float
        Significance necessary for a detection

    Returns
    ----------
    The relative flux neccessary to detect the source with the given target significance.
    '''
    is_scalar = np.isscalar(n_on) and np.isscalar(n_off)

    if is_scalar:
        n_on = [n_on]
        n_off = [n_off]

    scale = []
    for on, off in zip(n_on, n_off):
        if on < off*alpha or off == 0:
            scale.append(np.inf)
            continue

        def f(relative_flux):
            s = li_ma_significance((on - off) * relative_flux + off, off, alpha=alpha)
            return (target_significance - s)**2

        s = minimize_scalar(f, bounds=(1e-12, 100), method='bounded')

        scale.append(s.x)

    if is_scalar:
        return scale[0]
    return scale


class Spectrum():
    """
    A class containing usefull methods for working with power law spectra.
    """

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV,)
    def draw_energy_distribution(self, e_min, e_max, shape, index=None):
        if not index:
            index = self.index

        a = e_min.to('TeV').value**(index + 1)
        b = e_max.to('TeV').value**(index + 1)
        r = np.random.uniform(0, 1, shape)
        k = (a + (b - a) * r)
        e = k**(1. / (index + 1))
        return e * u.TeV

    @u.quantity_input(energy=u.TeV)
    def flux(self, energy):
        """
        Returns the (differential) flux of the spectrum at the given enrgy.
        """
        energy = energy.to('TeV')
        flux = self.normalization_constant * (energy / u.TeV)**(self.index)
        if self.generator_solid_angle:
            return flux.to(1 / (u.TeV * u.s * u.cm**2 * u.sr))
        else:
            return flux.to(1 / (u.TeV * u.s * u.cm**2))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events(self, e_min, e_max, area, t_obs):

        events = self._integral(e_min, e_max) * area * t_obs

        if self.generator_solid_angle:
            angle = self.generator_solid_angle.to('rad').value
            events = events * (1 - np.cos(angle)) * 2 * np.pi
            events = events * u.sr

        # at this point the value should have no units left
        # assert events.si.unit.is_unity() == True
        return events.si.value

    def _integral(self, e_min, e_max):
        a = e_min.to('TeV') / u.TeV
        b = e_max.to('TeV') / u.TeV

        index = self.index
        N = self.normalization_constant.to(1 / (u.TeV * u.s * u.m**2)) * u.TeV

        return N * (1 / (index + 1)) * (b**(index + 1) - a**(index + 1))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events_for_bins(
            self,
            e_min,
            e_max,
            area,
            t_obs,
            bins=10,
            log=True,
        ):
        """

        """
        if log:
            a = e_min.to('TeV').value
            b = e_max.to('TeV').value
            edges = np.logspace(np.log10(a), np.log10(b),
                                num=bins, base=10.0) * u.TeV
        else:
            edges = np.linspace(e_min, e_max, num=bins)

        events = []
        for e_low, e_high in zip(edges[0:], edges[1:]):
            e = self.expected_events(e_low, e_high, area, t_obs)
            events.append(e)

        events = np.array(events)
        return events, edges

    @u.quantity_input(
        event_energies=u.TeV,
        e_max_simulated=u.TeV,
        e_min_simulated=u.TeV,
        area=u.m**2,
        t_assumed_obs=u.h,
    )
    def weight(
            self,
            event_energies,
            e_min_simulated,
            e_max_simulated,
            area,
            t_assumed_obs,
            simulated_showers,
            simulated_index=-2.0,
        ):
        """
        This method returns weights for the given events based on the information
        about the events generator and the index and normalization of the spectrum.
        """
        event_energies = event_energies.to('TeV')
        e_min = e_min_simulated
        e_max = e_max_simulated

        gamma = -simulated_index
        w = event_energies**(gamma) * (e_max**(1 - gamma) -
                                       e_min**(1 - gamma)) / (1 - gamma)
        w = w * area * t_assumed_obs / simulated_showers
        if self.generator_solid_angle:
            angle = self.generator_solid_angle.to('rad').value
            w = w * (1 - np.cos(angle)) * 2 * np.pi * u.sr

        w = w * self.flux(event_energies)

        # at this point the value should have no units left
        # assert w.si.unit.is_unity() == True
        return w.si.value


class CrabSpectrum(Spectrum):
    '''
    The gamma ray energy spectrum of the Crab Nebula as measured by the HEGRA experiment.
    See Aharonian, F. et al. (HEGRA collaboration) 2004, ApJ 614, 897
    '''
    index = -2.62
    normalization_constant = 2.83e-14 / (u.GeV * u.cm**2 * u.s)
    generator_solid_angle = None


class CosmicRaySpectrum(Spectrum):
    index = -2.7
    normalization_constant = 9.6e-9 / (u.GeV * u.cm**2 * u.s * u.sr)
    generator_solid_angle = 6 * u.deg


class MCSpectrum(Spectrum):
    """
    A generic spectrum following a power law which can be used to get
    the number of simulated events generated by a Monte Carlo program
    or to reqeight simulated events to another generic spectrum.
    """
    index = -2.0
    generator_solid_angle = None
    normalization_constant = 1 / (u.TeV * u.m**2 * u.s)

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV)
    def __init__(self, e_min, e_max, total_showers_simulated, index=-2.0):
        """
        To calculate the normalization constant of this spectrum some
        information about the event generator has to be specified.
        """
        self.index = index
        N = self._integral(e_min.to('TeV'), e_max.to('TeV')) * (u.m**2 * u.s)
        self.normalization_constant = (
            total_showers_simulated / N) / (u.TeV * u.m**2 * u.s)


if __name__ == '__main__':
    # executing this will create a plot which is usefull for checking if
    # the reweighing works correctly
    import matplotlib.pyplot as plt
    s = CrabSpectrum()

    e_min = 0.003 * u.TeV
    e_max = 300 * u.TeV
    area = 1 * u.km**2
    N = 500000
    simulation_index = -2.0
    t_assumed_obs = 50 * u.h

    random_energies = s.draw_energy_distribution(
        e_min, e_max, N, index=simulation_index)

    # def efficiency(e):
    #     return lognorm.pdf(e, s=0.96, loc=0, scale=1)
    #
    # p = efficiency(random_energies)
    #
    # random_energies = np.random.choice(random_energies, size=N/2, replace=False, p=p/p.sum()) * u.TeV

    events, edges = s.expected_events_for_bins(
        e_min=e_min,
        e_max=e_max,
        area=area,
        t_obs=t_assumed_obs,
        bins=20
    )

    bin_center = 0.5 * (edges[:-1] + edges[1:])
    bin_width = np.diff(edges)

    plt.errorbar(
        bin_center.value,
        events,
        xerr=bin_width.value * 0.5,
        linestyle='',
        marker='.',
        label='expected events from crab',
        color='black',
    )
    plt.hist(
        random_energies,
        bins=edges,
        histtype='step',
        label='randomply sampled events with index {}'.format(
            simulation_index),
    )

    w = s.weight(random_energies, e_min, e_max, area=area, t_assumed_obs=t_assumed_obs,
                 simulated_showers=N, simulated_index=simulation_index)
    plt.hist(random_energies, bins=edges, histtype='step',
             weights=w, label='reweighted energies', color='red')

    plt.title('Event Reweighing')
    plt.suptitle('Red line should be on black points')
    plt.yscale('log')
    plt.xscale('log')
    plt.xlabel('Energy in TeV')
    plt.legend()
    plt.show()
