import click
import pandas as pd
import matplotlib.pyplot as plt
plt.style.use('ggplot')


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):
    df = pd.read_csv(input_file).dropna()

    dy = df['mc:core_y'] - df['stereo:estimated_impact_position:y']
    dx = df['mc:core_x'] - df['stereo:estimated_impact_position:x']

    plt.hist2d(dx, dy, bins=120, range=[[-800, 800], [-800, 800]], cmap='magma')
    plt.colorbar()

    plt.xlabel(r'$x_{\mathrm{True}} - x_{\mathrm{Estimated}}$')
    plt.ylabel(r'$y_{\mathrm{True}} - y_{\mathrm{Estimated}}$')

    plt.savefig(output_file)

if __name__ == "__main__":
    main()
