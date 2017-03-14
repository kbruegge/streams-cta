import click
import pandas as pd
import matplotlib.pyplot as plt
plt.style.use('ggplot')


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.argument('output_file', type=click.Path(exists=False))
def main(input_file, output_file):

    df = pd.read_json(input_file)

    for n, g in df.groupby('telescope_type'):
        plt.plot(g.telescope_position_x, g.telescope_position_y, '.', ms=7)

    plt.xlabel('x position in meters')
    plt.ylabel('y position in meters')
    plt.savefig(output_file)

if __name__ == "__main__":
    main()
