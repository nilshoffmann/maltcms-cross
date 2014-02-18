## The Factory

The main point of access within Cross is the `cross.IFactory` interface and its default implementation `cross.Factory`. It provides centralized 
access to all components that are necessary for the workflow, the pipeline, and the individual fragment commands. The object factory provided 
by the factory is a compatibility layer that uses functionality of the Spring IoC container under the covers. Additional functionality is provided
by the data source factory, which provides the known and registered io providers. The input data factory provides input data to 
the command pipeline, for example by parsing command line arguments. The fourth factory provided by the main factory is the file fragment factory,
which may be used to create new `FileFragment` instances. Finally, the main factory also provides access to the `ICvResolver` service which registers 
available providers of controlled vocabularies in the system and provides methods to resolve namespaced cv terms.

## Dependency Injection

Cross processing pipelines are assembled using the [Spring Inversion of Control container](http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/beans.html). The preferred format for configuration is xml-based, however the annotation and Java-based configuration mechanisms can also be used for more static application scenarios. 

## Linear Command Pipeline

The basic principle of a processing pipeline in Cross is that of a linear sequence of commands. 
The input and output of each command are file fragments. These can represent real 
files on the same or on a remote file system, or a specific set of records from a database. 

The pipeline itself is part of a workflow, which is responsible for high-level tasks, like event notification and bookkeeping.

Cross currently only offers IO provider implementations for file-based access. The [Maltcms](http://maltcms.sf.net) project
provides additional IO providers for the NetCDF, mzXML, mzData, and mzML formats.

## Unique Output Directories

The default setting of Cross is to create a new output directory per invocation of a pipeline. This 
can assure that no previously calculated results are accidentally overwritten, but it can also be 
a tremendous waste of space, unless an automatically deduplicating file system is used. 

By default, the output directory is created below the current user directory (`System.getProperty("user.dir")`), or, if set, below the directory given in the system property `output.basedir=/path/to/dir`. If the property `omitUserTimePrefix=true` is defined, output is directly placed into `output.basedir` by `cross.Factory.createCommandSequence`. Otherwise, output is placed below `System.getProperty("user.name", "default")` and within a directory named with the current timestamp of the workflow creation, with the following format `MM-dd-yyyy_HH-mm-ss` and US locale setting.

## Single Output Directory

It is also possible to specify a single output directory for repeated invocations of a pipeline. 
However, by default, Cross will issue an exception when it finds pre-existing files in the output directory.

### Overwriting

If the system property `output.overwrite=true` is set, Cross will not issue any warnings if existing files are 
replaced. Thus, this forced overwrite mode should be used with caution!

### Updating Existing Results

It is also possible to recalculate only certain parts of a pipeline by using the `ResultAwareCommandPipeline`. This requires to set `omitUserTimePrefix=true`, otherwise, the `ResultAwareCommandPipeline` behaves like the standard `CommandPipeline`. The pipeline performs 
automatic SHA-1 checksum calculations on input and output files of each fragment command and it calculates a recursive hash code on the parameters, annotated with `@Configurable` of each fragment command. If any of these values differs for a fragment command, the fragment command is executed again, as well as all fragment commands following it in the pipeline. The recursive hash code calculation requires that all annotated members of a fragment command explicitly implement `equals` and `hashCode` and that they adhere to the general [equals](http://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#equals%28java.lang.Object%29)/[hashCode](http://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#hashCode%28%29) contract required in Java. 

## Extension Points: Service Provider API

Cross uses the enhanced [NetBeans Lookup API](http://wiki.netbeans.org/AboutLookup) that is based on the JAVA service loader API to load available service implementations at runtime. 
Thus, classes providing a service interface implementation need to be annotated with `@ServiceProvider(service=IServiceThatIProvide.class)`. The 
necessary registration code is then created automatically during compilation.

