import click

import requests
import re
from ctapipe.io.hessio import hessio_event_source
from tqdm import tqdm
import os
import convert_raw_data
import gzip
import json


def download_file(folder, url, auth):
    '''
    See this stackoverflow post by Roman P.
    http://stackoverflow.com/questions/16694907/how-to-download-large-file-in-python-with-requests-py
    '''
    print('downloading from ' + url)

    local_filename = url.split('/')[-1]

    # remove gz extension
    local_filename = local_filename.split('.gz')[0]

    r = requests.get(url, stream=True, auth=auth)
    print(r.status_code)

    name = os.path.join(folder, local_filename)
    with open(name, 'wb') as f:
        for chunk in tqdm(r.iter_content(chunk_size=1024)):
            if chunk:  # filter out keep-alive new chunks
                f.write(chunk)

    return name


@click.command()
@click.argument('output_folder')
@click.option('--password', prompt=True, hide_input=True)
@click.option('--kind', default='proton', help='kind of files to download. either '
                                               'proton, gamma or electron')
def main(output_folder, password, kind):
    '''
    The OUTPUT_FOLDER argument specifies the path to the folder
    where the simtel files will be saved.
    '''

    url = 'https://www.mpi-hd.mpg.de/personalhomes/bernlohr/cta-raw/Prod-3/Paranal/Remerged-3HB8/'

    print('contacting server')
    r = requests.get(url, auth=('CTA', password))

    print('Status code: {}'.format(r.status_code))

    regex = r"href=\"({}.+?\.gz)".format(kind)
    links = re.findall(regex, r.text)

    for l in links:
        name = download_file(output_folder, url + l, auth=('CTA', password))
        outname = name.split('.simtel')[0] + '.json.gz'

        print('converting file to ' + outname)
        source = hessio_event_source(name)
        d = convert_raw_data.convert(source)

        with gzip.open(outname, mode='wt') as of:
            json.dump(d, of, indent=2)
            os.remove(name)


if __name__ == '__main__':
    main()
