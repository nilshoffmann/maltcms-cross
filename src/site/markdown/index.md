## Getting Started

Cross is a library providing a core of functionality involving dependency injection, workflow definition, validation, and execution, as well as 
efficient data structures for sequential and parallel data processing. It is used by [Maltcms and Maui](http://maltcms.sf.net) for the specific application domain of chromatography-mass spectrometry
data from analytical chemistry, metabolomics, and proteomics.

Cross is implemented in the platform-independent JAVA programming language. 
The individual modules of Cross are managed and built using [Maven](http://maven.apache.org).

If you want to use Cross in your own projects, please see the [Getting Started](./gettingStarted.html) page for more details.
Cross is dual-licensed under either the Lesser General Public License (L-GPL v3), or, at the licensees discretion, under the terms of the Eclipse Public License (EPL v1).

## Sequential Assembly

A workflow in Cross is made up of a sequence of _fragment command_
objects that use _file fragments_ as their in- and output type.
The number of in- and output _file fragments_ processed by a
_fragment command_ can differ, thus allowing _map-reduce_-like processing
schemes or generally schemes with different or equal parities.
The basic configuration of all workflow elements is performed using a Spring Application Context and
Spring Beans - based xml configuration, supplemented by runtime properties.

## Validation

Cross allows _fragment commands_ to define their required _variable fragments_ by 
adding class-level annotations. Additionally, _fragment commands_ may define which 
_variable fragments_ they provide. Thus, Cross can validate the accessibility of all
variables required by a workflow __before__ the workflow is actually executed. This 
helps avoid running computationally expensive workflows on invalid data.

## Monitoring and Transformation

A workflow monitors the _fragment commands_ it executes and notifies reqistered 
listeners of various workflow-related events. These include the creation of primary and 
secondary processing results, as well as general progress information. A workflow logs 
all completed tasks and their results in a distinct and unique (depending on configuration)
self-contained (except for initial input data) output directory. This output directory 
contains all information necessary to re-run the workflow with the exact same parameters 
and conditions. Workflows in Cross are therefore self-descriptive and repeatable. 

## Efficient Data Structures

A _file fragment_ is an aggregation of _variable fragment_ objects, defined by a
storage location URI. _File fragment_ objects may reference an arbitrary
number of _source files_, thereby allowing virtual aggregation of processing
result variables of previous fragment commands. _Shadowing_ allows _file fragments_
to hide the existence of an upstream variable of the same name from downstream
_file fragments_. _DataSource_ implementations allow different URI extensions to 
be handled, so that _file fragment_ objects can exist as simple files on disk or within 
a distributed database system. Custom implementations provide the mapping from binary 
or textual storage formats to the variable-based abstraction used by _Cross_.

## Caching of Intermediate Results

_File fragments_ have access to a user-defineable caching implementation. Currently,
_Ehcache_ (in memory and on disk), as well as a volatile in-memory weak reference hashmap and a non-volatile 
hashmap cache are available. Caches based on Ehcache can either be volatile or session-persistent, depending on the required use-case. Other 
cache implementations.

## Controlled Vocabulary

Cross variables have simple String-based names. However, in different contexts, the same 
variable name could have a different meaning. Thus, Cross supports namespaced controlled 
vocabularies for specific domains that translate a variable placeholder name to the 
actual, cv-supported clear name. The cv system also supports deprecation for the evolution of 
terms.

## Parallelization

Cross uses the [Mpaxs API](http://sf.net/p/mpaxs) for transparent parallelization
of _Runnable_ and _Callable_ tasks either within the local virtual machine or on 
other remote machines that are coordinated through _remote method invocation_(RMI).
Mpaxs therefore provides a standard _Executor_ and _Future_ compatible implementation 
to allow for easy scale-out of parallel jobs.
Scaling up and down with the required amount of parallelization can be managed 
automatically by Mpaxs for example using its OpenGridEngine (OracleGridEngine) compliant 
compute host launcher implementation. Mpaxs uses a round-robin scheduling 
method to utilize all available hosts as fair as possible.
