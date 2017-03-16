import Pyro4
import sys


@Pyro4.expose
class Processor(object):
    def process(self, name: str) -> str:
        return "Hello, {0}. Here is your fortune message:\n" \
               "Behold the warranty -- the bold print giveth and the fine "\
               "print taketh away.".format(name)


daemon = Pyro4.Daemon()
uri = daemon.register(Processor)

# print the uri so we can use it in the client later
print('{},{},{}\n'.format(uri.object, uri.host, uri.port))

# flush the output of print. because we read that output in the java thread.
# and it wont get printed otherwise.
sys.stdout.flush()
# start the event loop of the server to wait for calls from java
daemon.requestLoop()
