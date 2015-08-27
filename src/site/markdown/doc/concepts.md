# <a id="concepts"></a>Basic Concepts

The *streams* framework builds upon a flexible XML format, that can be
used to define various existing elements as well as incorporating custom
user-defined classes.

From a user-perspective, we basically want to have some tool for
designing continuous processes that will process data and provide
services that can be queried at any time. This basic view and the
*anytime* availability is a fundamental difference of stream
processing to traditionl data processing.

For designing such continuous processes, the *streams* framework
provides four abstract elements:

   - Container
   - Stream
   - Process
   - Service

The *container* element provides a life-cycle environment of our
processes whereas the *stream* elements define continuous sources of
data.

To reflect the processing and anytime service part, the *streams*
framework defines *process* and *service* elements.


## <a id="container"></a>Container

All elements of a stream process are defined within a *container* element.
A container is an environment where stream sources, services and processes
exist and are executed.
The following Figure 1 depicts the structure of a container and some of
the basic elements that can be added to it. 

<div class="figure">
   <img src="container-elements.png" style="height: 240px;"/>
   <p>Figure 1: Structure and elements of a container.</p>
</div>

A container can be started and will then create all the elements, such
as streams, queues, services and processes. The processes are reading
from inputs and will process all data items read from their inputs.

The most fundamental elements to design streaming processes are
`stream`, `process` and `service`.

## <a id="stream"></a>Notion of a Stream

A stream within the *streams* framework is a entity that provides
access to a sequence of data items. Data items a the atomic elements
that define a portion of data. This can be a record, a line in a
log-file, a twitter message or the like.

A data item is represented by a set of *(key,value)* pairs, a concept
known from almost all languages as Maps, Dictionaries or the like.
The keys in data items are plain strings, whereas the values may be
any serializable objects.

<div class="figure"><a id="fig:stream-items"></a>
   <img src="stream-items.png" style="height: 200px;" />
   <p>Figure 2: A stream is essential an (unbounded) sequence of data items.</p>
</div>

The *streams* framework already provides various stream implementations
for CSV data, JSON-encoded data, data in SVM-light format, data stored
in database tables and generation of synthetic data.

An overview of the different streams included in the stream framework
can be found in [Defining Streams](streams.html).

## <a id="process"></a>Processors and Services

The stream elements are by default passive entity, i.e. simply sources
that can be read from. This favors the *lazy evaluation* strategy
known from functional programming. To actually do any data processing,
a container needs to contain one or more *process* elements.

A process is an active element, that will read from a stream and execute
a set of processors for each item obtained from the stream until the stream
has ended. This is outlined in Figure 3:

<div class="figure">
   <img src="inside-process.png" style="height: 200px;"/>
   <p>Figure 3: A process applying processors to data items</p>
</div>

Each process consumes data items by reading from its associated input (a stream
or a queue) and executing *processors* on each of the data items. A *processor*
is a simple Java class that performs some function on a data item. 

The following class implements a complete processor, that prints out each data
item to the system output:

      public class Printer implements stream.Processor {
         public Data process( Data item ){
            System.out.println( item );
            return item;
         }
      }


## <a id="service"></a>Anytime Services

Anytime services are one of the key features of online processing over
traditional batch-processing of data. A service is essential a simple
set of functions that is exported to be accessible from anywhere at
anytime.

Services can be provided by *static* elements, i.e. a lookup-service
may provide functions that allow querying a database, or by processor
elements. For example, a learning-processor might be queried for
predictions of items based on its current model.

Within the *streams* framework, services are defined by Java
interfaces and can be implemented by plain Java classes. As a simple
example, the `EchoService` echoes a string upon request. This can
easily be defined as a service with the following Java interface:

      public interface EchoService implements stream.service.Service {
          public String echo( String str );
      }

To make a service available within a container, a class implementing
that service needs to be associated with an `id` as in the following
XML element:

       <service id="echo" class="my.package.EchoServiceImpl" />



