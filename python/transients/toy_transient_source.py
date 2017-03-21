# import pandas as pd
# import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import astropy.units as u
# from astropy.coordinates import cartesian_to_spherical, Angle
from scipy.stats import multivariate_normal, norm
from tqdm import tqdm


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


def create_cube(time_steps=100):
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


def main():
    slices = create_cube(time_steps=100)
    plot_cube(slices)

if __name__ == '__main__':
    main()
