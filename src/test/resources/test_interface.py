import Pyro4
# import sys


@Pyro4.expose
class Processor(object):
    def process(self, a, b):
        return a + b

    def add(self, a, b):
        return a + b


# daemon = Pyro4.Daemon()
# uri = daemon.register(Processor)
#
# # print the uri so we can use it in the client later
# print('{},{},{}\n'.format(uri.object, uri.host, uri.port))
#
# # flush the output of print. because we read that output in the java thread.
# # and it wont get printed otherwise.
# sys.stdout.flush()
# # start the event loop of the server to wait for calls from java
# daemon.requestLoop()


def main():
    Pyro4.Daemon.serveSimple(
            {
                Processor: 'streams.processors'
            },
            ns=True
    )

if __name__ == '__main__':
    main()
