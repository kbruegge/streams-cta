import click
from tqdm import tqdm
from ctapipe.io.hessio import hessio_event_source

import json
import gzip


def fill_camera_info(geom, telescope_type='LST', name='LSTCam'):
    c = {}
    c['number_of_pixel'] = len(geom.pix_x)
    c['pixel_x_positions'] = geom.pix_x.value.tolist()
    c['pixel_y_positions'] = geom.pix_y.value.tolist()
    c['pixel_rotation'] = geom.pix_rotation.value
    c['pixel_type'] = geom.pix_type
    c['pixel_ids'] = geom.pix_id.tolist()
    c['pixel_area'] = geom.pix_area.value.tolist()
    c['camera_rotation'] = geom.cam_rotation.value
    c['neighbours'] = geom.neighbors
    c['telescope_type'] = telescope_type
    c['name'] = name
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
    images = []
    for event in tqdm(source):
        d = {}
        for tel_id in event.dl0.tels_with_data:
            image = event.dl0.tel[tel_id].adc_sums[0].tolist()
            d[str(tel_id)] = image
        images.append(d)

    with open(output_file, 'w') as of:
        json.dump(images, of, indent=2)

if __name__ == '__main__':
    main()
