import click
from tqdm import tqdm
from ctapipe.io.hessio import hessio_event_source
from ctapipe.io.camera import guess_camera_geometry, _guess_camera_type

import json


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    '''
    The INPUT_FILE argument specifies the path to a simtel file. This script reads the
    array definition from there and puts them into a json file
    specified by OUTPUT_FILE argument.
    '''

    source = hessio_event_source(input_file)
    event = next(source)
    instruments = event.inst
    # num_tels = len(instruments.telescope_ids)

    d = []
    for tel_id in tqdm(instruments.telescope_ids):
        foc_len = instruments.optical_foclen[tel_id]

        num_pixels = len(instruments.pixel_pos[tel_id][0])
        telescope_type, name, _, _, _ = _guess_camera_type(num_pixels, foc_len)

        g = {}
        g['optical_focal_length'] = foc_len.value
        g['camera_name'] = name
        g['telescope_type'] = telescope_type
        g['telescope_position_x'] = instruments.tel_pos[tel_id][0].value
        g['telescope_position_y'] = instruments.tel_pos[tel_id][1].value
        g['telescope_position_z'] = instruments.tel_pos[tel_id][2].value

        d.append(g)

    with open(output_file, 'w') as of:
        json.dump(d, of, indent=2)

if __name__ == '__main__':
    main()
