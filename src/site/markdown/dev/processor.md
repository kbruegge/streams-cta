Implementing Custom Processors
==============================

Processors in the *streams* framework can be plugged into the
processing chain to perform a series of operations on the data.  A
processor is a simple element of work that is executed for each data
item. Essentially it is a simple function:

      public Data process( Data item ){
           // your code here
           return item;
      }

The notion of a processor is captured by the Java interface `stream.Processor`
that simply defines the `process(Data)` function mentioned above:

      public interface Processor {
          public Data process( Data item );
      }

Another property required for processors is that they need to provide a
*no-args* constructor, i.e. they need to have a constructor that comes
with no arguments.


Example: A simple custom processor
----------------------------------

In the following, we will walk through a very simple example to show
the implemenation of a processor in more detail. We will start with a
basic class and extend this to have a complete processor in the end.

The main construct is a Java class within some package:

      package my.package;

      public class Multiplier implements Processor {
           public Data process( Data item ){
               return item;
           }
      }

This class implements a processor that simply passes through each data
item to be further processed by all subsequent processors.


This simple processor is already ready to be used within a simple stream
processing chain. To use it, we can directly use the XML syntax of the *streams*
framework to include it in to the process:

       <container>
         ...
         <process input="...">
            <my.package.Multiplier />
         </process>
       </container>

### Processing data

The simple example shows the direct correspondence between the XML
definition of a container and the associated Java implemented
processors. The data items are represented as simple Hashmaps with
`String` keys and `Serializable` values.

The following extended code implements a data processor that checks
for the key `x` and adds the key `y` by multiplying `x` by 2:

     package my.package;

     import stream.Processor;
     import stream.data.Data;

     public class Multiplier implements Processor {

         /**
          * Extract the attribute x and add y as y = 2 * x
          * @param item
          */
         public Data process( Data item ){
             
             Serializable value = item.get( "x" );	     

             if( value != null  ){

                // parse a double-value from the string representation
                //
                Double x = new Double( value.toString() );
                
                // add the value 2*x as new attribute of the item:
                //
                data.put( "y",  new Double(  2 * x ) );
             }
             return item;
         }
     }


This simple multiplier relies on parsing the double value from its string
representation. If the double is available as Double object already in the
item, then we could also use a cast for this:

     // directly cast the serializable value to a Double object:
     //
     Double x = (Double) item.get( "x" );

The multiplier will be created at the startup of the experiment and will be
called (i.e. the `process(..)` method) for each event of the data stream.


### Adding Parameters

In most cases, we want to add a simple method for parameterizing our Processor
implementation. This can easily be done by following the Convention&Configuration
paradigm:

By convention, all `setX(...)` and `getY()` methods are automatically regarded as
parameters for the data processors and directly available as XML attributes.

In the example from above, we want to add two parameters: `key` and `factor` to
our Multiplier implementation. The `key` parameter will be used to select the
attribute used instead of `x` and the `factor` will be a value used for multipying
(instead of the constant `2` as above).

To add these two parameters to our Multiplier, we only need to provide corresponding
getters and setters:

        String key = "x";    // by default we still use 'x'
        Double factor = 2;   // by default we multiply with 2

        // getter/setter for parameter "key"
        //
        public void setKey( String key ){
            this.key = key;
        }

        public String getKey()(
            return key;
        }

        // getter/setter for parameter "factor"
        // 
        public void setFactor( Double fact ){
            this.factor = fact;
        }

        public Double getFactor(){
            return factor;
        }
        
After compiling this class, we can directly use the new parameters `key` and `factor`
as XML attributes. For example, to multiply all attributes `z` by `3.1415`, we can
use the following XML setup:

       <container>
           ...
           <process input="...">
               <my.package.Multiplier key="z" factor="3.1415" />
            </process>
       </container>

Upon startup, the getters and setters of the Multiplier class will be checked and
if the argument is a Double (or Integer, Float,...) it will be automatically converted
to that type.

In the example of our extended Multiplier, the `factor` parameter will be created to
a Double object of value *3.1415* and used as argument in the `setFactor(..)` method.



