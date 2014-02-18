Release Notes

Changes for version 1.3.1:
---------------------------

New features

* Added ResultAwareCommandPipeline, see [the documentation](./doc/concepts.html) for more details
* Added user and system time to measured metrics. Added defensive measures to avoid Exceptions.
* Added method to compare two arrays for (element) equality.
* Added AutoRetrievalSoftReferenceCache.
* Updated and extended JavaDoc API documentation.
* Further improvements in test coverage.

Deprecation

* The singleton static access pattern for `cross.Factory().getInstance()` has been deprecated in favor of using 
`cross.IFactoryService` and its corresponding implementation `cross.FactoryService`. Cross 1.4 wll not use this pattern 
any longer.

Bug fixes

* Fixes to ResultAwareCommandPipeline and DefaultWorkflow result to be aware of prematurely terminated workflows.
* Fix for deepest ancestor being the file fragment itself.
* Fixed a bug in FileFragment externalization that closed in/out streams too early.
* Fixed cache providers to use thread safe collections.
* Fixed fragment cache creation errors and cache manager warnings.
* Fixed creation of fragment command xmls.
* Fixed versioning problems for nbm modules.
* Fixed netcdf nbm cluster definition to export additional package.

Updates

* Updated lombok version.
* Updated mpaxs dependency.
* Changed default FileFragment cache type to NONE.

Changes for version 1.3:
---------------------------

New features

* Cross artifacts now expose osgi dependency information and can be deployed 
in osgi-compatible runtime containers. The cross-osgi module lists all required 
dependencies.
* `java.net.URI` is now consistently used by cross
for all `cross.datastructures.fragments.IFileFragment` instances.
This also enables access to remote file resources, as long as they may
be opened using an input stream.
* `cross.datastructures.fragments.FileFragment` now uses a custom breadh-first
search through its ancestors for localization of variables.
* The cross-cache module has been added and multiple
different caches are now available. The `ucar.ma2.Array` class has been
supplemented with a Serialization helper used by Ehcache for disk-based caching.
* `cross.vocabulary` has been added to allow for programmatic resolution of
variable name placeholders. This new feature has been integrated into `cross.commands.fragments.IFragmentCommand`
and `cross.commands.fragments.AFragmentCommand`.
* `cross.applicationContext.DefaultApplicationContextFactory` now supports creation of a context from classpath resources
in addition to file system resources.
* More and more descriptive exceptions and improved documentation.
* A large number of tests.

Removed

* The module cross-ui has been removed.

This version is backwards compatible to 1.2.x releases of cross concerning the workflow format.

Changes for version 1.2.11:
---------------------------
New features

* `cross.applicationContext`
    * `ConfiguringBeanPostProcessor`
      Applies to instances of `cross.IConfigurable`. Please note that
      `configure(Configuration cfg)` is called AFTER instantiation and
      configuration of the objects by the application context. Thus,
      settings provided via an xml configuration file could be overridden by
      an entry in the global properties-based configuration. Implementors
      should thus only use the configuration interface for custom
      initialization that can not be performed otherwise using spring.
* `cross.datastructures.fragments`
    * `VariableFragment`
      Does not support lazy loading anymore, use it only for data creation.
      Uses a cache implementation using db4o local database storage to avoid
      keeping large arrays in memory.
    * `CachedList`
      Fixed an issue, when `CachedList` would not reset the range of the
      referenced index variable.

Deprecated Classes

* `cross.Logging` has been removed. Please use `@Slf4j` annotation on your
  classes and use the field `log.[info,warn,debug]` etc. for your logging
  statements.
* `cross.datastructures.fragments.IVariableFragment.getVarname()` is
  deprecated and references to it have already been removed throughout cross
  and maltcms. Please use `getName()` instead.
