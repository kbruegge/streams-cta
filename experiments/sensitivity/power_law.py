import numpy as np
import astropy.units as u
from scipy.stats import lognorm


class Spectrum():

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV,)
    def draw_energy_distribution(self, e_min, e_max, shape, index=None):
        if not index:
            index = self.index

        a = e_min.to('TeV').value**(index + 1)
        b = e_max.to('TeV').value**(index + 1)
        r = np.random.uniform(0, 1, shape)
        k = (a + (b - a)*r)
        e = k**(1./(index + 1))
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
        event_energies = event_energies.to('TeV')
        e_min = e_min_simulated
        e_max = e_max_simulated
        #
        # expected_events = self.expected_events(
        #             e_min_simulated,
        #             e_max_simulated,
        #             area,
        #             t_obs=1*u.s
        #         )
        # #
        # s = MCSpectrum(e_min, e_max, simulated_index, simulated_showers)
        # w = s.expected_events(e_min, e_max)/simulated_showers * (event_energies/u.TeV)**(-simulated_index)
        # w = w * self.flux(event_energies) * area * u.TeV * u.s
        # w = w/expected_events
        #
        # from IPython import embed; embed()
        #

        # event weights to reskew the energy spectrum
        gamma = -simulated_index
        w = event_energies**(gamma) * (e_max**(1 - gamma) - e_min**(1 - gamma)) / (1 - gamma)
        w = w * area * t_assumed_obs / simulated_showers
        if self.generator_solid_angle:
            angle = self.generator_solid_angle.to('rad').value
            w = w * (1 - np.cos(angle)) * 2 * np.pi * u.sr
        # from IPython import embed; embed()
        w = w * self.flux(event_energies)/1000

        # w = area * event_energies**(gamma) * (e_max**(1 - gamma) - e_min**(1 - gamma)) / (1 - gamma) * t_assumed_obs / simulated_showers # * angular_thing
        # from IPython import embed; embed()
        # print('expected_events {}'.format(expected_events))
        # print('sum of weights: {}'.format(np.sum(w)))

        assert w.si.unit.is_unity() == True
        return w.si.value


class CrabSpectrum(Spectrum):
    index = -2.62
    normalization_constant = 2.83e-14/(u.GeV * u.cm**2 * u.s)
    generator_solid_angle = None


class CosmicRaySpectrum(Spectrum):
    index = -2.7
    normalization_constant = 9.6e-9 / (u.GeV * u.cm**2 * u.s * u.sr)
    generator_solid_angle = 6 * u.deg


class MCSpectrum(Spectrum):
    generator_solid_angle = None

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV)
    def __init__(self, e_min, e_max, index, total_showers_simulated):
        self.index = index
        self.normalization_constant = 1
        self.normalization_constant = (total_showers_simulated /self._integral(e_min.to('GeV'), e_max.to('GeV'))).value * (1 / (u.GeV * u.m**2 * u.s))

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV)
    def expected_events(self, e_min, e_max):

        events = self._integral(e_min, e_max) * 1*u.m**2 * 1 * u.s

        assert events.si.unit.is_unity() == True
        return events.si.value

    @u.quantity_input(e_min=u.TeV, e_max=u.TeV)
    def expected_events_for_bins(
                    self,
                    e_min,
                    e_max,
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
            e = self.expected_events(e_low, e_high)
            events.append(e)

        events = np.array(events)
        return events, edges


if __name__ == '__main__':
    import matplotlib.pyplot as plt
    s = CrabSpectrum()

    e_min = 0.003*u.TeV
    e_max = 300*u.TeV
    area = 1*u.km**2
    N = 500000
    simulation_index = -2.0
    t_assumed_obs = 50 * u.h

    random_energies = s.draw_energy_distribution(e_min, e_max, N, index=simulation_index)
    #
    def efficiency(e):
        return lognorm.pdf(e, s=0.96, loc=0, scale=1)

    p = efficiency(random_energies)

    # from IPython import embed; embed()
    random_energies = np.random.choice(random_energies, size=N/2, replace=False, p=p/p.sum()) * u.TeV
    # triggered_event_energies = random_energies
    # print(random_energies)
    # random_energies = random_energies[random_energies > 10*u.TeV]

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


    w = s.weight(random_energies, e_min, e_max, area=area, t_assumed_obs=t_assumed_obs, simulated_showers=N, simulated_index=simulation_index)
    plt.hist(random_energies, bins=edges, histtype='step', weights=w, label='reweighted energies', color='red')

    plt.title('Event Reweighing')
    plt.suptitle('Red line should be on black points')
    plt.yscale('log')
    plt.xscale('log')
    plt.xlabel('Energy in TeV')
    plt.legend()
    plt.show()
