## Linear Workflow

The basic principle of a workflow in Cross is that of a linear sequence of commands. 
The input and output of each command are file fragments. These can represent real 
files on the same or on a remote file system, or a specific set of records from a database. 

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
