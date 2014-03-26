## Developing with Cross

### Requirements

Cross is implemented in JAVA 7. The source code is available at [SourceForge](http://sf.net/p/maltcmscross)

- [Java 7 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html?ssSourceSiteId=otnjp)
- [Git](http://git-scm.com/)
- [Apache Maven](http://maven.apache.org/), and/or an IDE with integrated Maven support

### Building Cross from Source 

The sourcecode of Cross is under control of the distributed version control system [Git](http://git-scm.com/).
In order to access the latest source, you need to clone the repository to your local system:

    
    git clone git://git.code.sf.net/p/maltcmscross/code maltcmscross-code
    

Other software for working with Git should work similarly. 

After checking out, you will find the root maven module cross below maltcmscross-code. 
Run

    
    mvn install
    

from the command line to build the complete project, to run the corresponding unit tests, and to install 
the generated artifacts into your local maven repository.

### Using Cross as a Library

The modules of Cross are published as maven artifacts, hosted 
by a custom [Artifactory](http://www.jfrog.com/home/v_artifactory_opensource_overview) instance
at [Maltcms Artifactory](http://maltcms.de/artifactory).

To add the Maltcms Artifactory to your JAVA project, please follow [these instructions for your 
specific build system](http://maltcms.de/artifactory/webapp/mavensettings.html).

For Maven projects, you should add the following repository definition below the _repositories_ tag 
in your _pom.xml_

    <repositories>
        ...
        <repository>
            <id>maltcms-artifactory-release</id>
            <layout>default</layout>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>daily</updatePolicy>
            </snapshots>
            <url>http://maltcms.de/artifactory/repo</url>
        </repository>
    </repositories>

Please note that you can only retrieve artifacts from this repository without authentication that have 
already been deployed. You can not use the artifactory as a proxy to other repositories.
