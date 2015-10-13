
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
   <img style="margin:auto; height: 200px;" src="architecture2.png" />
   <p>Figure 1: Components of the <code>streams</code> library.</p>
</div>

Figure 1 shows the components of the `streams` library. The binding glue element
is a thin API layer that attaches to a runtime provided as a separate module or
can embedded into existing code.


