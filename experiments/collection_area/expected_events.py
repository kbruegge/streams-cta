import numpy as np


def power_spectrum(index, N, bin_edges, log=True):
    events = []
    for e_low, e_high in zip(bin_edges[0:], bin_edges[1:]):
        if log:
            e_low, e_high = 10**e_low, 10**e_high

        e = (1 / (index + 1)) * e_high**(index + 1) - \
            (1 / (index + 1)) * e_low**(index + 1)
        events.append(e)

    events = np.array(events)
    total = np.sum(events)

    return N / total * events
