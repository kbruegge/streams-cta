# Services &amp; Naming

Each container may be provided with a simple identifier using the
`id` attribute. This defines a namespace for the container. As an
example, the following XML defines a container called `myContainer`:

      <container id="myContainer">
         ...
      </container>

Any *service*, that is defined within this container, will be registered
and can be accessed with a name like `//myContainer/serviceId`.
The definition of a static service may for instance look like:

      <container id="myContainer">

         <service id="myService" class="my.package.MyService" />

      </container>


## <a id="services"></a>Services

A service requires two elements to be available: a Java interface defining
the methods of the service and a class implementing that interface. The
interface needs to extend the `stream.service.Service` interface.

The following piece of Java code is a valid service definition:

      public interface EchoService extends stream.service.Service {
           public String echo( String str );
      }


### Consuming a Service

The service concepts of course depends on a consumer side, which makes use of
the service provided. In the *streams* framework, the consumer may simply
provide a `set`-method for the service.

The following piece of Java code is required to use the `EchoService` defined
above:

     public class EchoConsumer implements Processor {

         EchoService echoService;

         public void setService( EchoService echoService ){
             this.echoService = echoService;
         } 
         ...
     }

With this processor as a Service consumer, we can use the service in a
container definition as follows:

      <container id="myContainer">
           <service id="echo" class="my.package.EchoServiceImpl" />

           <process input="some-stream">
              <my.package.EchoConsumer service="echo" />
           </process>
      </container>

When the container is started, the `EchoServiceImpl` class will be instantiated
and registered as service with id `echo`. Before the processes are started,
all services will be injected using `set`-methods that obtain service interfaces.