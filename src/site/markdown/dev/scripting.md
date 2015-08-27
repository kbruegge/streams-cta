Scripting
=========

Scripting languages provide a convenient way to integrate ad-hoc
functionality into your stream-process. Based on the Java Scripting
Engine, the streams library includes support for several scripting
languages, most notably the JavaScript language.

Currently the following scripting languages are supported:

   - JavaScript
   - JRuby (if jruby-library provided in classpath)

Further support for integrating additional languages is planned.


Using JavaScript
----------------

The JavaScript language has been part of the Java API for some
time. The *streams* framework provides a simple `JavaScript`
processor, that can be used to run JavaScript functions on data items:

       <container>
          ...
          <process input="...">

              <stream.script.JavaScript file="/path/to/myScript.js" />

          </process>
       </container>


Within the JavaScript environment, the data items are accessible at
`data`. Below is a simple example of JavaScript code within the file
`myScript.js`:

       function process(data){
          var id = data.get( "@id" );
          if( id != null ){
             println( "ID of item is: " + id );
          }
          return data;
        }