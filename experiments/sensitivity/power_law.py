import numpy as np
import astropy.units as u

class Spectrum():

    solidangle = 1
    index = -2.0
    normalization_constant = 1 * 1 / (u.GeV * u.cm**2 * u.s)
    e_norm = 1 * u.GeV

    def __init__(self, solidangle=1):
        self.solidangle = solidangle

    def _rate(self, e, area, t_obs):
        return (self.flux(e * u.TeV) * area.to('m^2') * t_obs.to('s')).value

    def _energy_normalization(self, index, simulated_index, e_min, e_max):

        e_0 = (1 + index) / (1 + simulated_index) * \
            ((e_max**(1 + simulated_index)) - (e_min**(1 + simulated_index))) \
            / ((e_max**(1 + index)) - (e_min**(1 + index)))

        e_0 = e_0**(1 / (simulated_index - index))
        return e_0

    @u.quantity_input(energy=u.TeV)
    def flux(self, energy):
        energy = energy.to('TeV')
        flux = self.normalization_constant * (energy/u.TeV)**(self.index)
        return flux.to(1/(u.cm**2 * u.s * u.TeV))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events(self, e_min, e_max, area, t_obs):
        norm = self.normalization_constant
        index = self.index

        e_max = e_max.to('TeV')
        e_min = e_min.to('TeV')

        flux = norm/(1 + index) * \
            ((e_max/u.TeV)**(self.index + 1) - (e_min/u.TeV)**(self.index + 1)) * 1*u.TeV

        expected_events = flux * area * t_obs
        assert expected_events.decompose().unit.is_unity() is True

        return expected_events.decompose().value

    @u.quantity_input(e_min=u.GeV, energies=u.GeV, e_max=u.GeV, simulation_area=u.m**2, t_obs=u.h)
    def event_weights(self, energies, simulated_showers, e_min, e_max, simulation_area, simulated_index=-2.0, t_obs=50 * u.h):
        index = self.index

        e_0 = self._energy_normalization(
                index,
                simulated_index,
                e_min.to('TeV').value,
                e_max.to('TeV').value,
            ) * u.TeV

        expected_events = self.expected_events(e_min, e_max, simulation_area, t_obs)
        weight = (energies / e_0)**(index - simulated_index) * expected_events / simulated_showers
        assert weight.decompose().unit.is_unity() is True

        return weight


class CrabSpectrum(Spectrum):

    def __init__(self, solidangle=1):
        self.index = -2.62
        self.normalization_constant = 2.83E-14 * (1 / (u.GeV * u.s * u.cm**2))
        self.solidangle = solidangle


class CosmicRaySpectrum(Spectrum):

    def __init__(self, solidangle=1):
        self.index = -2.7
        solidangle = 2 * np.pi * (1 - np.cos(np.deg2rad(6))) * u.sr
        self.normalization_constant = 9.6E-9 * (1 / (u.GeV * u.s * u.sr * u.cm**2)) * solidangle
        self.solidangle = solidangle
