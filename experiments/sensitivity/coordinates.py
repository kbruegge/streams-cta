import astropy.units as u
from astropy.coordinates import cartesian_to_spherical, Angle, SkyCoord
from astropy.coordinates import EarthLocation, Latitude, Longitude
from dateutil import parser


@u.quantity_input(alt=u.rad, az=u.rad, source_alt=u.rad, source_az=u.rad)
def distance_to_source(alt, az, source_alt, source_az):
    lat = Latitude((24, 37, 38), unit='deg')
    lon = Longitude((70, 34, 15), unit='deg')
    paranal = EarthLocation.from_geodetic(lon, lat, 2600)
    # paranal = EarthLocation.of_site('paranal')
    dt = parser.parse('2017-09-20 22:15')

    c = SkyCoord(
        alt=alt,
        az=az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    c_source = SkyCoord(
        alt=source_alt,
        az=source_az,
        obstime=dt,
        frame='altaz',
        location=paranal,
    )

    return c.separation(c_source)


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
