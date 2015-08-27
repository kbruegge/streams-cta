Maven Integration
=================

The *streams* library is implemented as a multi-module Maven
project. Its modules are available as artifacts in the central Maven
repository.

The most important artifacts of the *streams* library are

  - `streams-api`
  - `streams-core`
  - `streams-runtime`

All of these artifacts are rather small and ideal for being embedded
into existing applications.


*streams* API
-------------

The API module is the core artifact, which defines the interfaces and
basic classes required for running stream processes. The `streams-api`
dependency is:

       <dependency>
           <groupId>org.jwall</groupId>
           <artifactId>streams-api</artifactId>
           <version>[0.9.11,)</version>
       </dependency>

