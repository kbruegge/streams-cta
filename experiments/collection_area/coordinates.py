import astropy.units as u
from astropy.coordinates import cartesian_to_spherical, Angle, SkyCoord, EarthLocation
from dateutil import parser


@u.quantity_input(mc_alt=u.rad, mc_az=u.rad, x=u.m, y=u.m, z=u.m)
def distance_between_estimated_and_mc_direction(x, y, z, mc_alt, mc_az):

    r, lat, lon = cartesian_to_spherical(x, y, z)

    alt = Angle(90 * u.deg - lat)
    mc_alt = Angle(mc_alt)

    az = Angle(lon).wrap_at(180 * u.deg)
    mc_az = Angle(mc_az).wrap_at(180 * u.deg)

    paranal = EarthLocation.of_site('paranal')
    dt = parser.parse('2017-09-20 22:15')

    c = SkyCoord(
        alt=alt,
        az=az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    c_mc = SkyCoord(
        alt=mc_alt,
        az=mc_az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    return c.separation(c_mc)
