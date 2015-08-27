

Overview
--------

The `streams` framework comprises several modules, of which the central one is the
*stream-api* module. The *stream-api* defines the basic interfaces and data structures
used in all other modules.

The basic modules are
  
  * [stream-api](stream-api/index.html) -- the basic API interfaces and classes. This
    module is used as glue element, new modules/extensions can include this module as
    their base dependency.
    Among the basic interfaces, the stream-api module provides a very small number of 
    generic processors, e.g. for executing JavaScript or JRuby (if the jruby library
    is available in the class path).

  * [stream-core](stream-core/index.html) -- a library providing various preprocessing
    functions (processors) as well as several data stream sources (stream.io) for
    reading from CSV files, SVMlight format, URLs, etc.

  * [stream-runtime](stream-runtime/index.html) -- this modules provides a generic
    runtime environment for setting up and running stream processes that have been
    defined in an XML file.

The modules are again maven projects, each having a separate project page and
documentation.



Source Code & Usage
-------------------

The source code of the framework is available at [bitbucket.org](https://bitbucket.org/cbockermann/streams/).

Each of the modules can easily be integrated into your own code and used as library by
listing it as maven dependency. The libraries are currently available via the following
maven repository:

      <repository>
         <id>jwall</id>
         <name>jwall.org Maven Repository</name>
         <url>http://secure.jwall.org/maven/repository/all</url>
      </repository>
