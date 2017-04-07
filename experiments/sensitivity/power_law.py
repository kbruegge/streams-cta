import numpy as np
import astropy.units as u
from scipy.integrate import quad


@u.quantity_input(e_min=u.TeV, e_max=u.TeV, energy=u.TeV)
def power_law(
        energy,
        N,
        e_min=0.003*u.TeV,
        e_max=300*u.TeV,
        index=-2.0,
        collection_area=np.pi*3000**2 * u.m**2,
        ):
    total = integral_of_power_spectrum(e_min.to('TeV').value, e_max.to('TeV').value, index)
    return ((N / total) * (energy/u.TeV)**index) / (u.TeV * collection_area)


class Spectrum():

    def _f(self, e, area, t_obs):
        return (self.rate(e*u.TeV) * area.to('m^2') * t_obs.to('s')).value

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events(self, e_min, e_max, area, t_obs):
        a = e_min.to('TeV').value
        b = e_max.to('TeV').value

        events, _ = quad(lambda e: self._f(e, area, t_obs), a, b)
        return events

    @u.quantity_input(energies=u.TeV)
    def event_weights(self, energies, simulated_showers, expected_showers, index_mc):
        ew = (energies/u.TeV)**(np.abs(index_mc) - np.abs(self.index))
        return (ew/np.sum(ew)) * expected_showers/simulated_showers


class CrabSpectrum(Spectrum):
    index = -2.48

    @u.quantity_input(energy=u.TeV)
    def rate(self, energy):
        ''' function for a pseudo-Crab point-source rate
        Crab source rate:   dN/dE = 3e-7  * (E/TeV)**-2.48 / (TeV * m² * s)
        (watch out: unbroken power law... not really true)
        norm and spectral index reverse engineered from HESS plot...

        Parameters
        ----------
        energy : astropy quantity
            energy for which the rate is desired

        Returns
        -------
        flux : astropy quantity
            differential flux at E

        '''
        return 3e-7 * (energy/u.TeV)**-2.48 / (u.TeV * u.m**2 * u.s)


class CosmicRaySpectrum(Spectrum):
    index = -8.0/3.0

    @u.quantity_input(energy=u.TeV)
    def rate(self, energy):
        ''' function of the cosmic ray spectrum (simple power law, no knee/ankle)
        Cosmic Ray background rate: dN/dE = 0.215 * (E/TeV)**-8./3 / (TeV * m² * s * sr)
        norm and spectral index reverse engineered from "random" CR plot...

        Parameters
        ----------
        energy : astropy quantity
            energy for which the rate is desired

        Returns
        -------
        flux : astropy quantity
            differential flux at E

        '''
        return 100 * 0.1**(8./3) * (energy/u.TeV)**(-8./3) / (u.TeV * u.m**2 * u.s * u.sr)

    def expected_events(self, e_min, e_max, area, t_obs):
        e = super(CosmicRaySpectrum, self).expected_events(e_min, e_max, area, t_obs) * (1-np.cos((6*u.deg).to('rad')))*2 * np.pi
        return e.value


def integral_of_power_spectrum(e_low, e_high, index, N=1):
    return N * ((1 / (index + 1)) * e_high**(index + 1) - (1 / (index + 1)) * e_low**(index + 1) )


@u.quantity_input(event_energies=u.GeV, e_min=u.GeV, e_max=u.GeV)
def expected_events_for_bins(
                N,
                event_energies,
                bins=10,
                index=-2.0,
                e_min=3.0*u.GeV,
                e_max=330000*u.GeV,
            ):

    _, edges = np.histogram(np.log10(event_energies.to('GeV').value), bins=bins)

    events = []
    for e_low, e_high in zip(edges[0:], edges[1:]):

        e_low, e_high = 10**e_low * u.GeV, 10**e_high * u.GeV

        e = integral_of_power_spectrum(e_low.to('GeV').value, e_high.to('GeV').value, index)
        events.append(e)

    events = np.array(events)

    total = integral_of_power_spectrum(e_min.to('GeV').value, e_max.to('GeV').value, index)
    return N / total * events, edges
