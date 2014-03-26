## Maven project modules

The Cross Maven project is a multi-module project. The following sections explain 
the rationale behind each module and describe their functions. More info on the concepts 
used in Cross can be found [here](./concepts.html).

## [Cross Annotations](../cross-annotations/index.html)

This module contains the annotations used for annotating fragment commands and 
individual member variables. It additionally provides classes to query and inspect
a given object/class for annotations present on it.

[Module JavaDoc](../cross-annotations/apidocs/index.html)

## [Cross Cache](../cross-cache/index.html)

The cache module contains implementations for the `ICacheDelegate` interface and 
provides implementations backed by Ehcache, soft references, or a plain in memory map.

[Module JavaDoc](../cross-cache/index.html)

## [Cross Events](../cross-events/index.html)

The workflow in cross main provides event notification to registered listeners 
throughout its execution. The basic event types are contained in the cross events module.

[Module JavaDoc](../cross-events/apidocs/index.html)

## [Cross Exceptions](../cross-exceptions/index.html)

Cross uses a number of custom exceptions to signal constraint violations, unavailability
of resources, unavailable cv mappings and non-recoverable errors. The exceptions are all 
unchecked and thus require no mandatory exception catching.

[Module JavaDoc](../cross-exceptions/apidocs/index.html)
 
## [Cross Main](../cross-main/index.html)

The main module contains the most important interfaces and abstract implementations for the 
runtime system, workflow creation and execution, as well as essential datastructures, like 
file and variable fragments. Additionally, it provides basic support for IO, controlled vocabularies,
and configuration.

[Concepts](./concepts.html)
[Module JavaDoc](../cross-main/apidocs/index.html)

## [Cross Math](../cross-math/index.html)

The math module provides classes for set operations on standard Java sets, as well as 
classes to support the systematic enumeration of all unique and non-repetitive combinations 
of elements from different partitions. 

[Module JavaDoc](../cross-math/apidocs/index.html)

## [Cross Test](../cross-test/index.html)

The test module provides custom JUnit 4 rules that can be used in test classes for test method logging 
and log framework (slf4j and log4j) setup. 

[Module JavaDoc](../cross-test/apidocs/index.html)

## [Cross Tools](../cross-tools/index.html)

The tools module provides general purpose classes for string handling, introspection, and mathematical functions.

[Module JavaDoc](../cross-tools/apidocs/index.html)
