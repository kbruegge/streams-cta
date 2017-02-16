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


def fill_mc_dict(event):
    mc = {}
    mc['energy'] = event.mc.energy.value
    mc['alt'] = event.mc.alt.value
    mc['az'] = event.mc.az.value
    mc['core_x'] = event.mc.core_x.value
    mc['core_y'] = event.mc.core_y.value
    return mc


def fill_array_dict(event):
    d = {}
    tels = [t.tolist() for t in event.dl0.tels_with_data]
    d['triggered_telescopes'] = tels
    d['num_triggered_telescopes'] = len(tels)
    return d


def fill_images_dict(event):
    img_dict = {}
    for tel_id in event.dl0.tels_with_data:
        image = calibrate(event, tel_id)
        img_dict[str(tel_id)] = image.tolist()

    return img_dict


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
    data = []
    event_id = 0
    for event in tqdm(source):
        # from IPython import embed
        # embed()
        c = {}
        c['images'] = fill_images_dict(event)
        c['mc'] = fill_mc_dict(event)
        c['array'] = fill_array_dict(event)
        c['event_id'] = event_id
        event_id += 1

        data.append(c)

    with open(output_file, 'w') as of:
        json.dump(data, of, indent=2)

if __name__ == '__main__':
    main()
