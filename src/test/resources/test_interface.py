import Pyro4


@Pyro4.expose
class Processor(object):
    def process(self, item):
        return item

    def add_thing_to_item(self, item):
        item['thing'] = 'an awesome thing'
        return item

    def add(self, a, b):
        return a + b


def main():
    Pyro4.Daemon.serveSimple(
            {
                Processor: 'streams.processors'
            },
            ns=True
    )

if __name__ == '__main__':
    main()
