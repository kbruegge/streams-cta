# Distributed Processing

The concept of containers introduced so far has been focusing on
self-contained containers. One of the reasons for the
*container*-concept, is to provide a possible split of streaming
processes into multiple containers, which can be deployed in a
distributed fashion.

Two elements are central for distribution: the concept of remote
services and a naming scheme. Both have essentially been introduced
for local elements in section [Processes &amp;
Services](../doc/concepts.html#process).

By extending the naming scheme to incorporate the container
identifiers, we extend this to inter-container communication as shown
in Figure 1.

<div class="figure">
   <img src="remote-queue.png" style="height: 180px;" />
   <p>Figure 1: Inter-Container communcation between `crawler` and `storage`.</p>
</div>


## <a id="remote-container"></a> Distributing Containers

In the simplest case, a container is self-contained and will execute by
itself. However, elements within a container may reference elements in other
containers, allowing for a distributed setup of processes.

A very simple example is given by the two containers in Figure 2. The
container `storage` defines a queue and a process that will store all
elements from that queue in a database.

The second container `crawler` reads data items from Twitter and sends these
to the input queue of the `storage` container.

<div class="figure">
   <img src="crawler-storage.png" style="height: 200px;" />
   <p>
     Figure 2: Two simple crawler and storage containers.
   </p>
</div>

### <a id="broadcast-discover"></a> Automatic Container Descovery

By default, each container makes itself available via RMI and responds
to braodcast queries. Therefore no configuration is required as long
as the network infrastructure is capable of distributing the
broadcasts (e.g. in a single ethernet segment).


### <a id="container-discovery"></a> Defining Remote Container Connections

In some situation, the broadcast discover cannot be used or may be
unreliable. To deal with these situations, the naming-service of the
*streams* library allows for manually defining references to remote
containers.

The following Figure 3 shows the `crawler` container with an explicit
RMI reference to the `storage` container.


<div class="figure">
   <img src="distributed/crawler-explicit-ref.png" />
   <p>
     Figure 2: Two simple crawler and storage containers.
   </p>
</div>
