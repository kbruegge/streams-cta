import click
from tqdm import tqdm
from ctapipe.io.hessio import hessio_event_source

import json
# import gzip


def calibrate(event, tel_id):
    CALIB_SCALE = 1.05

    samples = event.dl0.tel[tel_id].adc_samples

    if len(samples) > 0:
        # not astrii
        n_samples = samples.shape[2]
        pedestal = event.mc.tel[tel_id].pedestal / n_samples
        gain = event.mc.tel[tel_id].dc_to_pe * CALIB_SCALE
        calibrated_samples = (samples - pedestal[..., None]) * gain[..., None]
        # sum up first gain channel
        image = calibrated_samples[0].sum(axis=1)
        return image

    else:
        n_samples = 1
        samples = event.dl0.tel[tel_id].adc_sums
        pedestal = event.mc.tel[tel_id].pedestal / n_samples
        gain = event.mc.tel[tel_id].dc_to_pe * CALIB_SCALE
        calibrated_samples = (samples - pedestal) * gain
        # return first gain channel
        return calibrated_samples[0]


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
            image = calibrate(event, tel_id)
            d[str(tel_id)] = image.tolist()
        images.append(d)

    with open(output_file, 'w') as of:
        json.dump(images, of, indent=2)

if __name__ == '__main__':
    main()
