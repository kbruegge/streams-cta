import matplotlib.pyplot as plt
from matplotlib import animation
import numpy as np
import astropy.units as u
from scipy.stats import multivariate_normal, norm
from tqdm import tqdm
import pywt
plt.style.use('ggplot')


def background(lam=2, size=[80, 80]):
    background = np.random.poisson(lam=lam, size=size)
    return background


def signal(
            signal_location=np.array([61, 21])*u.deg,
            fov_center=np.array([60, 20])*u.deg,
            width=0.01*u.deg,
            signal_events=80,
            bins=[80, 80],
            fov=4*u.deg,
        ):
    # reshape so if signal_events = 1 the array can be indexed in the same way.
    signal = multivariate_normal.rvs(
                mean=signal_location.value,
                cov=width.value,
                size=signal_events
                ).reshape(signal_events, 2)
    r = np.array([fov_center - fov/2, fov_center + fov/2]).T

    signal_hist, _, _ = np.histogram2d(signal[:, 0], signal[:, 1], bins=bins, range=r)
    return signal_hist


def transient(time_steps, width=0.05, max_events=100):
    t = np.linspace(0, 1, num=time_steps)
    # normalize so that the maximum is at max_events
    g = norm.pdf(t, loc=0.5, scale=width)
    g = (g / g.max()) * max_events

    # and pull poissonian from it
    events = np.random.poisson(lam=g)
    return t, events


def create_cube_with_transient(time_steps=100):
    slices = []
    transient_location = np.array([59, 20])*u.deg
    time, values = transient(time_steps, max_events=700, width=0.08)
    for t, v in tqdm(zip(time, values)):
        H = background() + signal(signal_events=280)
        transient_signal = signal(
                        signal_location=transient_location,
                        signal_events=v,
                        width=0.01*u.deg
                        )
        H = H + transient_signal
        slices.append(H)
    return np.array(slices)


def create_cube(time_steps=100):
    slices = []
    for i in tqdm(range(100)):
        H = background() + signal(signal_events=280)
        slices.append(H)
    return np.array(slices)


def plot_cube(slices):
    plt.ion()
    fig, ax = plt.subplots()

    quad = ax.pcolormesh(slices[0], cmap='viridis', vmin=0, vmax=slices.max())
    fig.colorbar(quad)
    plt.show()

    for m in slices:
        quad.set_array(m.ravel())
        # quad.autoscale() this would rescale the colorbar in each frame of the animation
        fig.canvas.draw_idle()
        plt.pause(0.01)


class Plotter(object):

    def __init__(self, left_cube, right_cube, trans_factor):
        self.fig = plt.figure()

        ax1 = plt.subplot2grid((2, 2), (0, 0))
        ax2 = plt.subplot2grid((2, 2), (0, 1))
        ax3 = plt.subplot2grid((2, 2), (1, 0), colspan=2)

        ax1.tick_params(labelbottom='off', labelleft='off')
        ax2.tick_params(labelbottom='off', labelleft='off')

        ax3.set_xlabel('Time Step in a.u.')
        ax3.set_ylabel('Trigger Criterion in a.u.')

        vmax = left_cube.max()
        self.l_quad = ax1.pcolormesh(left_cube[0], cmap='viridis', vmin=0, vmax=vmax)
        self.r_quad = ax2.pcolormesh(left_cube[0], cmap='viridis', vmin=0, vmax=vmax)

        self.line,  = ax3.plot(0, trans_factor[0])

        ax3.set_xlim([0, len(trans_factor)])
        ax3.set_ylim([0, trans_factor.max()])

        self.left_cube = left_cube
        self.right_cube = right_cube
        self.trans_factor = trans_factor
        self.x = []
        self.y = []

    def step(self, t):
        self.x.append(t)
        self.y.append(self.trans_factor[t])

        l = self.left_cube[t]
        r = self.right_cube[t]
        self.l_quad.set_array(l.ravel())
        self.r_quad.set_array(r.ravel())
        self.line.set_data(self.x, self.y)

        return [self.l_quad, self.r_quad, self.line]


def thresholding(
            coefficient_list,
            sigma_d=2,
            k=3,
            kind='hard',
            sigma_levels=[0.889, 0.2, 0.086, 0.041, 0.020, 0.010, 0.005, 0.0025, 0.0012]):
    '''
    Here we just iterate over all the coefficents and remove those under a certain
    threshold using the pywt.threshold method.
    '''

    r = []
    for level, cs in enumerate(coefficient_list):
        d = {}
        for key, v in cs.items():
            if key == 'aaa':
                d[key] = v
            else:
                d[key] = pywt.threshold(v, sigma_d*k*sigma_levels[level], kind)
        r.append(d)

    return r


def main():
    time_steps = 100
    cube_steady = create_cube(time_steps=time_steps)
    cube_with_transient = create_cube_with_transient(time_steps=100)

    # remove mean measured noise from current cube
    cube = cube_with_transient - cube_steady.mean(axis=0)
    coeffs = pywt.swtn(data=cube, wavelet='bior1.3', level=2,)

    # remove noisy coefficents.
    ct = thresholding(coeffs, k=30)
    cube_smoothed = pywt.iswtn(coeffs=ct, wavelet='bior1.3')

    trans_factor = cube_smoothed.max(axis=1).max(axis=1)

    p = Plotter(cube_with_transient, cube_smoothed, trans_factor)

    anim = animation.FuncAnimation(
            p.fig,
            p.step,
            frames=time_steps,
            interval=15,
            blit=True,
        )

    anim.save('animation.gif', writer='imagemagick', fps=25)

if __name__ == '__main__':
    main()
