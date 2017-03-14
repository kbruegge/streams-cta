import click
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
plt.style.use('ggplot')


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    df = pd.read_csv(input_file).dropna()

    estimation = df['prediction:energy:mean']
    energy = df['mc:energy']

    plt.hist2d(np.log10(energy*1000), np.log10(estimation*1000), bins=100, cmap='inferno')
    plt.colorbar()
    plt.xlabel(r'$\log_{10}(\frac{\mathrm{Energy}} { \mathrm{GeV}} )$ ')
    plt.ylabel(r'$\log_{10}(\frac{\mathrm{Estimated Energy}} { \mathrm{GeV}} )$ ')

    plt.savefig(output_file)

if __name__ == "__main__":
    main()
