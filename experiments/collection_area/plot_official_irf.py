import matplotlib.pyplot as plt
from astropy.io import fits
import numpy as np

hdulist = fits.open('./irf_file.fits')

area = hdulist[1].data['EFFAREA'][0]

# convert to GeV
e_low = hdulist[1].data['ENERG_LO']*1000
e_high = hdulist[1].data['ENERG_HI']*1000

bin_edges = np.append(e_low[0], e_high[0][-1])

bin_center = 0.5 * (bin_edges[:-1] + bin_edges[1:])
bin_width = np.diff(bin_edges)

fig, ax = plt.subplots(1)
ax.errorbar(
            bin_center,
            area[0],
            xerr=bin_width * 0.5,
            marker='.',
            linestyle='',
            capsize=0,
    )
ax.set_yscale('log')
ax.set_xscale('log')

ax.set_ylabel(r'$Area / \mathrm{m}^2$')
ax.set_xlabel(r'$E /  \mathrm{GeV}$')

plt.savefig('../../build/official_prod3_area.pdf')
