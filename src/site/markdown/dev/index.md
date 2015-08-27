Custom Processor Development
============================

One of the big strenghts of the *streams* library is its easy extendability. By simply
implementing the `Processor` interface, users can quickly create a new Java class that
can instantly be integrated into XML configurations.

Additionally, the *streams* library allows for the direct integration of JavaScript
code to process items. The following snippet uses JavaScript to multiply the `x`
attribute of all data items:

        <stream.script.JavaScript>
             function process(item){
             	var value = item.get( "x" );
             	item.put( "x", 2*value );
             	return item;
             }
        </stream.script.JavaScript>

More examples can be found in [Implementing Processors](processors.html) and [Processor Scripts](scripting.html).