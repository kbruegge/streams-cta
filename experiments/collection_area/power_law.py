import numpy as np
import astropy.units as u


def integral_of_power_spectrum(e_low, e_high, index):
    return (1 / (index + 1)) * e_high**(index + 1) - \
        (1 / (index + 1)) * e_low**(index + 1)


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
