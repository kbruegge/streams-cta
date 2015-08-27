

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
