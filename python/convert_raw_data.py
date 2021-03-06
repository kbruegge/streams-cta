import click
from tqdm import tqdm
from ctapipe.io.hessio import hessio_event_source
import gzip
import json


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

    # below is the calibration in case of astrii
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


def convert(source):
    data = []
    event_id = 0
    for event in tqdm(source):
        c = {}
        c['images'] = fill_images_dict(event)
        c['mc'] = fill_mc_dict(event)
        c['array'] = fill_array_dict(event)
        c['event_id'] = event_id
        event_id += 1

        data.append(c)

    return data


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
@click.option('--limit', default=100, help='number of events to convert from the file.'
                                           'If a negative value is given, the whole file'
                                           'will be read')
def main(input_file, output_file, limit, compress):
    '''
    The INPUT_FILE argument specifies the path to a simtel file. This script reads the
    camera definitions from there and puts them into a json file
    specified by OUTPUT_FILE argument.
    '''

    if limit > 0:
        source = hessio_event_source(input_file, max_events=limit)
    else:
        source = hessio_event_source(input_file)

    data = convert(source)

    with open(output_file, 'w') as of:
        json.dump(data, of, indent=2)


if __name__ == '__main__':
    main()
