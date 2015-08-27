Defining Streams
================

Stream objects are the sources of data within the *streams* library. A
stream is essentially an object that data items can be read from. Each
read call on a stream should reveal a new data item or *null*, if the
stream has ended.

The *streams* library includes a variety of different implementations
for creating streams based on different data formats, such as comma
separated data (CSV), sparse vectors in
[SVMlight](http://svmlight.joachims.org) format and others.


The <code>stream</code> element
-------------------------------

The element to define a stream within a container is called
`stream`. It requires a `class` attribute, which specifies the
implementation to use and a `url` attribute for the data
source. Depending on the specific implementation, other parameters
might be required.

In addition, an `id` attribute is required to associate the stream
with a name and connect it to some process.

The following snippet shows the definition of a CSV stream from
a HTTP url:

      <stream id="myStream" class="stream.io.CsvStream"
              url="http://www.jwall.org/streams/sample.csv" />


### Reading compressed Data

Most of the provided stream implementations can auto-detect and
handle GZIP-compression. If the URL ends with `.gz`, then the source
is expected to be GZIP-compressed and is uncompressed on the fly,
e.g.:

      <stream id="compressedCSV" class="stream.io.CsvStream" 
              url="http://www.jwall.org/streams/sample.csv.gz" />


### Limiting Streams

By default, stream elements are read until no more data is
available. Every stream element provides a `limit` parameter to cut
off the stream after a specified amount of items have been read (or
earlier, if the stream ends before the limit is reached).

To create a stream of the first 10000 elements of the CSV source
mentioned above, we simply need to add the `limit` attribute:

      <stream id="compressedCSV" class="stream.io.CsvStream" 
              url="http://www.jwall.org/streams/sample.csv.gz"
              limit="10000" />


MultiStreams
============

There exists a set of special stream implementations that can be
used to combine different streams into one. These streams are called
*MultiStreams* and different implementations are available.

In the simplest case, a MultiStream sequentially reads a list of
streams. The following example shows a stream that reads 1000 elements
of the CSV data mentioned above, followed by 1000 elements of some
other source:

      <stream id="multi" class="stream.io.multi.SequentialMultiStream">
   
          <stream id="firstStream" class="stream.io.CsvStream" 
                  url="http://www.jwall.org/streams/sample.csv.gz"
                  limit="1000" />
   
          <stream id="secondStream" class="stream.io.CsvStream" 
                  url="http://www.jwall.org/streams/second-example.csv.gz"
                  limit="1000" />
      </stream>

The MultiStream adds an additional attribute `@source:id` to the data
items, which holds the ID of the stream from which it has been read.
The name of this source attribute can be changed by adding the
`sourceKey` attribute to the MultiStream definition:

      <stream id="multi" class="stream.io.multi.SequentialMultiStream"
              sourceKey="@stream-id">   
           <!-- your sub-streams here ... -->
      </stream>
  
