Custom Data Stream Implementations
==================================

The *streams* framework already provides a couple of generic stream
implementations. All of these implement the [`stream.io.DataStream`](../stream-api/apidocs/stream/io/DataStream.html)
interface.

Custom data streams (as well as MultiStreams) can easily be added by
re-using the
[`AbstractDataStream`](../stream-api/apidocs/stream/io/AbstractDataStream.html)
class or directly implements the
[`DataStream`](../stream-api/apidocs/stream/io/DataStream.html)
interface.


The <code>DataStream</code> interface
-------------------------------------

The `DataStream` interface mainly provides a single method
`readNext(Data)` to read the next data item from some underlying data
source. DataStream imlementations are required to provide a
constructor for a `URL` object or an `InputStream` object to read
from.

In addition the interface requires implemenations to provide an
`init()` and a `close()` method for opening the underlying stream and
thoroughly closing it upon process completion.


Even more Data: <code>MultiStream</code>
----------------------------------------

Whereas the `DataStream` represents a single stream of data, the
`MultiStream` class extends this to a possible set of different
sub-streams.

The class
[`AbstractMultiStream`](../stream-core/apidocs/stream/io/multi/AbstractMultiStream.html)
provides an elementary implementation of a MultiStream that only
requires for the `readNext(Data,Map)` method to be implemented:

       public Data readNext( Data item, Map<String,DataStream> substreams ) {
           //
           // implement your multi-stream logic and fill the data item
           //
           return item;
       }
