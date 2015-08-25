@0x9eb32e19f86ee174;
using Java = import "java.capnp";
$Java.package("streams.cta.io.capnproto");
$Java.outerClassname("RawCTAEvent");

struct Event {
  telescopeId @0 :Int32;
  numPixel @1 :Int32;
  roi @2 :Int32;
  type @3 :Text;
  samples @4 :List(Int16);
}