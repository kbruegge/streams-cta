import numpy as np
import astropy.units as u


class Spectrum():

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV,)
    def draw_energy_distirbution(self, e_min, e_max, shape):
        a = e_min.to('TeV').value**self.index
        b = e_max.to('TeV').value**self.index
        r = np.random.uniform(a, b, shape)
        g = self.index
        e = (r * (g + 1)/self.normalization_constant)**(1.0/(g+1))
        return e*u.TeV

    @u.quantity_input(energy=u.TeV)
    def flux(self, energy):
        energy = energy.to('TeV')
        flux = self.normalization_constant * (energy/u.TeV)**(self.index)
        if self.generator_solid_angle:
            return flux.to(1/(u.TeV * u.s * u.cm**2 * u.sr))
        else:
            return flux.to(1/(u.TeV * u.s * u.cm**2))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV, area=u.m**2, t_obs=u.s)
    def expected_events(self, e_min, e_max, area, t_obs):

        events = self._integral(e_min, e_max) * area * t_obs

        if self.generator_solid_angle:
            angle = self.generator_solid_angle.to('rad').value
            events = events * (1 - np.cos(angle)) * 2 * np.pi
            events = events * u.sr

        assert events.si.unit.is_unity() == True
        return events.si.value

    def _integral(self, e_min, e_max):
        a = e_min.to('TeV')/u.TeV
        b = e_max.to('TeV')/u.TeV

        index = self.index
        N = self.normalization_constant * u.GeV

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
        if log:
            a = e_min.to('TeV').value
            b = e_max.to('TeV').value
            edges = np.logspace(np.log10(a), np.log10(b), num=bins, base=10.0) * u.TeV
        else:
            edges = np.linspace(e_min, e_max, num=bins)

        events = []
        for e_low, e_high in zip(edges[0:], edges[1:]):
            e = self.expected_events(e_low, e_high, area, t_obs)
            events.append(e)

        events = np.array(events)
        return events, edges

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV)
    def _correction_factor(self, e_min, e_max, simulated_index=-2.0):
        index = self.index

        e_min = e_min.to('TeV')/u.TeV
        e_max = e_max.to('TeV')/u.TeV

        k = (1 + index)/(1 + simulated_index)
        events_simulated = (e_max**(1 + simulated_index) - e_min**(1. + simulated_index))
        events_from_spectrum = (e_max**(1 + index) - e_min**(1. + index))

        e_0 = k * events_simulated / events_from_spectrum
        return e_0 * u.TeV

    @u.quantity_input(
        event_energies=u.TeV,
        e_max_simulated=u.TeV,
        e_min_simulated=u.TeV,
        area=u.m**2
    )
    def weight(
            self,
            event_energies,
            e_min_simulated,
            e_max_simulated,
            area,
            simulated_showers,
            simulated_index=-2.0
            ):
        event_energies = event_energies.to('TeV')

        e_0 = self._correction_factor(e_min_simulated, e_max_simulated, simulated_index)

        rate = self.expected_events(e_min_simulated, e_max_simulated, area, t_obs=1*u.s)\
            / u.s

        w = (event_energies / e_0)**(self.index - simulated_index) \
            * rate / simulated_showers

        return w.to(1/u.s)


class CrabSpectrum(Spectrum):
    index = -2.62
    normalization_constant = 2.83e-14/(u.GeV * u.cm**2 * u.s)
    generator_solid_angle = None


class CosmicRaySpectrum(Spectrum):
    index = -2.7
    normalization_constant = 9.6e-9 / (u.GeV * u.cm**2 * u.s * u.sr)
    generator_solid_angle = 6 * u.deg
