import click
from tqdm import tqdm
from ctapipe.io.hessio import hessio_event_source
from ctapipe.io.camera import guess_camera_geometry, _guess_camera_type

import json


def fill_camera_info(geom, telescope_type='SST'):
    c = {}
    c['number_of_pixel'] = len(geom.pix_x)
    c['pix_x_meter'] = geom.pix_x.value.tolist()
    c['pix_y_meter'] = geom.pix_y.value.tolist()
    c['pix_rotation_degree'] = geom.pix_rotation.value
    c['pix_type'] = geom.pix_type
    c['pix_id'] = geom.pix_id.tolist()
    c['pix_area_square_meter'] = geom.pix_area.value.tolist()
    c['cam_rotaion_degree'] = geom.cam_rotation.value
    c['neighbors'] = geom.neighbors
    c['telescope_type'] = telescope_type
    return c


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    '''
    The INPUT_FILE argument specifies the path to a simtel file. This script reads the
    camera definitions from there and puts them into a json file
    specified by OUTPUT_FILE argument.
    '''

    source = hessio_event_source(input_file)
    event = next(source)
    instruments = event.inst

    d = {}

    for tel_id in tqdm(instruments.telescope_ids):
        pix_x = instruments.pixel_pos[tel_id][0]
        pix_y = instruments.pixel_pos[tel_id][1]
        foc_len = instruments.optical_foclen[tel_id]
        geom = guess_camera_geometry(pix_x, pix_y, foc_len)

        num_pixels = len(pix_x)
        telescope_type, name, _, _, _ = _guess_camera_type(num_pixels, foc_len)

        d[name] = fill_camera_info(geom, telescope_type)

    with open(output_file, 'w') as of:
        json.dump(d, of, indent=2)

if __name__ == '__main__':
    main()
