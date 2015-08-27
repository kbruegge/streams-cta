
The <code>streams</code> Framework
=======================

The `streams` framework is a Java implementation of a simple stream processing
environment. It aims at providing a clean and easy-to-use Java-based platform to
process streaming data.

The core module of the `streams` library is a thin API layer of interfaces and
classes that reflect a high-level view of streaming processes. This API serves
as a basis for implementing custom processors and providing services with the
`streams` library.

<div style="margin:auto; border: none; text-align: center;">
   <img style="margin:auto; height: 200px;" src="doc/architecture2.png" />
   <p>Figure 1: Components of the <code>streams</code> library.</p>
</div>

Figure 1 shows the components of the `streams` library. The binding glue element
is a thin API layer that attaches to a runtime provided as a separate module or
can embedded into existing code.


### Process Design with JavaBeans


The `streams` library promotes simple software design patterns such as JavaBean
conventions and dependency injection to allow for a quick setup of streaming
processes using simple XML files.

As shown in Figure 2, the idea of the `streams` library is to provide a simple
runtime environment that lets users define streaming processes in XML files,
with a close relation to the implementing Java classes.

<div style="margin:auto; border: none; text-align: center;">
   <img style="margin:auto; height: 200px;" src="doc/process-design.png" />
   <p style="margin-left:20px; margin-right:20px;">Figure 2: XML process definitions mapped to a runtime environment, using 
   stream-api components and other libraries.</p>
</div>

Based on the conventions and patterns used, components of the
`streams` library are simple Java classes.  Following the basic design
patterns of the `streams` library allows for quickly adding custom
classes to the streaming processes without much trouble.

