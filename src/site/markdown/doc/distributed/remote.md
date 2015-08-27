# Remote Containers

The generic naming service scheme using `//container-id/service-id`
has been used for abstracting from any low-level definition of
inter-container communication.  By default, this communication is
being auto-established using broadcasts and transported using plain
Java RMI calls.


### Starting a Container with Remote Capabilities

A container requires its IP-address to be specified before being
accessible from remote or accessing other remote containers. For
this, we need to either define the container address in the XML
definition

     <container address="192.168.128.13">
        ....
     </container>

or specify the `container.address` property on the commandline:

    # java -Dcontainer.address=192.168.128.13 -jar stream-runner.jar ...

    

### <a id="broadcast-discover"></a> Automatic Container Discovery

When a container is started with an address provided, it makes itself
available via RMI and responds to broadcast queries. Containers
referencing remote containers will send broadcasts at startup time to
established remote connections.

Therefore no configuration is required as long as the network
infrastructure is capable of distributing the broadcasts (e.g. in a
single ethernet segment).




### <a id="container-discovery"></a> Defining Remote Container Connections

In some situation, the broadcast discover cannot be used or may be
unreliable. To deal with these situations, the naming-service of the
*streams* library allows for manually defining references to remote
containers.

The following Figure 3 shows the `crawler` container with an explicit
RMI reference to the `storage` container.


<div class="figure">
   <img src="crawler-explicit-ref.png" />
   <p>
     Figure 2: Two simple crawler and storage containers.
   </p>
</div>
