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
    '''
    A class containing usefull methods for working with power law spectra.
    This class should be subclassed with real , physical, values for
    the normalization constants. This class has a constant of 1 /(TeV m^2 h)
    and and index of -1.0. Not very usefull by itself.

    See the subclasses `~power_law.CosmicRaySpectrum` and `~power_law.CrabSpectrum`
    for usefull physicall spectra.
    '''
    index = -1
    normalization_constant = 1 / (u.TeV * u.m**2 * u.h)
    extended_source = False

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV,)
    def draw_energy_distribution(self, e_min, e_max, shape, index=None):
        '''
        Draw random energies from a power_law spectrum.
        It is different from the scipy powerlaws because it supports negative indeces.
        Parameters
        ----------
        e_min:  Quantity
            lower energy bound
        e_max: Quantity
            upper energy bound
        size: int
            number of random values to pick.

        Returns
        ----------
        An array of random numbers, with energy units (TeV) attached, of the given size.
        '''
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
        '''
        Returns the (differential) flux of the spectrum at the given enrgy.
        '''
        energy = energy.to('TeV')
        flux = self.normalization_constant * (energy / u.TeV)**(self.index)
        if self.extended_source:
            return flux.to(1 / (u.TeV * u.s * u.cm**2 * u.sr))
        else:
            return flux.to(1 / (u.TeV * u.s * u.cm**2))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events(self, e_min, e_max, area, t_obs, solid_angle=None):
        '''
        Get the number of events which are expected to arrive from this spectral source.
        So its basically the integral of the flux within the given energy bounds.

        Parameters
        ----------
        e_min:  Quantity
            lower energy bound
        e_max: Quantity
            upper energy bound
        area: Quantity
            area over which particles are counted
        t_obs: Quantity
            observation time over which is being integrated
        solid_angle: Quantity (optional)
            the solid angle from which events are detected.
            Not needed for non-extended sources.
        '''
        events = self._integral(e_min, e_max) * area * t_obs

        if self.extended_source:
            if not solid_angle:
                raise ValueError('solid angle needs to be specified for extended sources')

            angle = solid_angle.to('rad').value
            events = events * (1 - np.cos(angle)) * 2 * np.pi
            events = events * u.sr

        # at this point the value should have no units left
        # assert events.si.unit.is_unity() == True
        return events.si.value

    def _integral(self, e_min, e_max):
        a = e_min.to('TeV') / u.TeV
        b = e_max.to('TeV') / u.TeV

        index = self.index

        if self.extended_source:
            N = self.normalization_constant.to(1 / (u.TeV * u.s * u.m**2 * u.sr)) * u.TeV
        else:
            N = self.normalization_constant.to(1 / (u.TeV * u.s * u.m**2)) * u.TeV

        return N * (1 / (index + 1)) * (b**(index + 1) - a**(index + 1))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events_for_bins(
            self,
            e_min,
            e_max,
            area,
            t_obs,
            solid_angle=None,
            bins=10,
            log=True,
        ):
        '''
        Get the number of events which are expected to arrive from this spectral source.
        For each of the requested bins.
        Parameters
        ----------
        e_min:  Quantity
            lower energy bound
        e_max: Quantity
            upper energy bound
        area: Quantity
            area over which particles are counted
        t_obs: Quantity
            observation time over which is being integrated
        solid_angle: Quantity (optional)
            the solid angle from which events are detected.
            Not needed for non-extended sources.
        bins: int
            The number of bins to create between e_min and e_max.
        log: boolean
            Whether to use logarithmically spaced bins or not.
            So when log == True this will use np.logpsace to create the bins.

        '''
        if log:
            a = e_min.to('TeV').value
            b = e_max.to('TeV').value
            edges = np.logspace(np.log10(a), np.log10(b),
                                num=bins, base=10.0) * u.TeV
        else:
            edges = np.linspace(e_min, e_max, num=bins)

        events = []
        for e_low, e_high in zip(edges[0:], edges[1:]):
            e = self.expected_events(e_low, e_high, area, t_obs, solid_angle=solid_angle)
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
            mc_spectrum,
            t_assumed_obs,
            ):
        '''
        This method returns weights for the given events based on the information
        about the events generator and the index and normalization of the spectrum.
        '''
        event_energies = event_energies.to('TeV')
        e_min = mc_spectrum.e_min
        e_max = mc_spectrum.e_max

        gamma = -mc_spectrum.index
        w = event_energies**(gamma) * (e_max**(1 - gamma) - e_min**(1 - gamma)) \
            / (1 - gamma)
        w = w * mc_spectrum.generation_area\
            * t_assumed_obs / mc_spectrum.total_showers_simulated

        if self.extended_source:
            angle = mc_spectrum.generator_solid_angle.to('rad').value
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


class CosmicRaySpectrum(Spectrum):
    '''
    BESS Proton spectrum  ApJ 545, 1135 (2000) [arXiv:astro-ph/0002481],
    same as used by K. Bernloehr.

    I stole this from the MARS Barcelona code provided by Tarek. H.
    '''
    index = -2.7
    normalization_constant = 9.6e-9 / (u.GeV * u.cm**2 * u.s * u.sr)
    extended_source = True


class MCSpectrum(Spectrum):
    '''
    A generic spectrum following a power law which can be used to get
    the number of simulated events generated by a Monte Carlo program
    or to reweight simulated events to another generic spectrum.
    '''
    index = -2.0
    generator_solid_angle = None
    normalization_constant = 1 / (u.TeV * u.m**2 * u.s)
    extended_source = False

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, generation_area=u.m**2)
    def __init__(
            self,
            e_min,
            e_max,
            total_showers_simulated,
            generation_area,
            generator_solid_angle=None,  # default for CTA prod3
            index=-2.0
            ):
        '''
        To calculate the normalization constant of this spectrum some
        information about the event generator has to be specified.

        Parameters
        ----------
        e_min:  Quantity
            Minimun energy simulated
        e_max: Quantity
            Maximum energy simulated
        total_showers_simulated: int
            Total number of showers that have been simulated
        generation_area: Quantity
            The total area over which the primary particles are scattered.
            Also know as the maximum_impact_distance**2 * pi.
        generator_solid_angle: Quantity
            The solid angle over which the particles were created.
            This is necessary for extended sources like the cosmic ray spectrum
        '''
        self.e_min = e_min
        self.e_max = e_max
        self.total_showers_simulated = total_showers_simulated
        self.index = index
        self.generation_area = generation_area
        self.generator_solid_angle = generator_solid_angle
        self.normalization_constant = 1 / (u.TeV * u.m**2 * u.s)
        if generator_solid_angle:
            self.extended_source = True
            self.normalization_constant = 1 / (u.TeV * u.m**2 * u.s * u.sr)
            angle = generator_solid_angle.to('rad').value
            angle = (1 - np.cos(angle)) * 2 * np.pi * u.sr

            N = self._integral(e_min.to('TeV'), e_max.to('TeV')) * (generation_area.to(u.m**2) * u.s * angle)
            self.normalization_constant = (
                    total_showers_simulated / N
                ) / (u.TeV * u.m**2 * u.s * u.sr)

        else:
            N = self._integral(e_min.to('TeV'), e_max.to('TeV')) * (generation_area.to(u.m**2) * u.s)
            self.normalization_constant = (
                    total_showers_simulated / N
                ) / (u.TeV * u.m**2 * u.s)


if __name__ == '__main__':
    # executing this will create a plot which is usefull for checking if
    # the reweighing works correctly
    import matplotlib.pyplot as plt

    e_min = 0.003 * u.TeV
    e_max = 300 * u.TeV
    area = 1 * u.km**2
    N = 5000000
    simulation_index = -2.0
    t_assumed_obs = 50 * u.h

    mc = MCSpectrum(
        e_min=e_min,
        e_max=e_max,
        total_showers_simulated=N,
        generation_area=area,
    )

    random_energies = mc.draw_energy_distribution(
        e_min, e_max, N, index=simulation_index)

    # def efficiency(e):
    #     return lognorm.pdf(e, s=0.96, loc=0, scale=1)
    #
    # p = efficiency(random_energies)
    #
    # random_energies = np.random.choice(random_energies, size=N/2, replace=False, p=p/p.sum()) * u.TeV

    s = CrabSpectrum()
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
        label='randomply sampled events with index {}'.format(simulation_index),
    )

    w = s.weight(random_energies, mc_spectrum=mc, t_assumed_obs=t_assumed_obs)
    plt.hist(random_energies, bins=edges, histtype='step',
             weights=w, label='reweighted energies', color='red')

    plt.title('Event Reweighing')
    plt.suptitle('Red line should be on black points')
    plt.yscale('log')
    plt.xscale('log')
    plt.xlabel('Energy in TeV')
    plt.legend()
    plt.show()
