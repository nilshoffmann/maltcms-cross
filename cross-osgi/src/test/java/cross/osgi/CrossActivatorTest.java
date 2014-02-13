package cross.osgi;

import cross.test.SetupLogging;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;

/**
 *
 * @author Nils Hoffmann
 */
@Ignore
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class CrossActivatorTest {

    @Rule
    public SetupLogging setupLogging = new SetupLogging();
//    @Inject
//    private BundleContext bc;
//    @Inject
//    private Factory factory;

    @Configuration
    public Option[] config() {

        return options(
            junitBundles(),
            mavenBundle("net.sf.maltcms", "cross-osgi", "1.3.1-SNAPSHOT").start(),
            //            mavenBundle("net.sf.maltcms", "cross-main", "1.3.1-SNAPSHOT").start(),
            mavenBundle("net.sf.maltcms", "cross-test", "1.3.1-SNAPSHOT").start()//,
        //            mavenBundle("net.sf.maltcms", "cross-annotations").versionAsInProject(),
        //            mavenBundle("net.sf.maltcms", "cross-exceptions").versionAsInProject(),
        //            mavenBundle("net.sf.maltcms", "cross-cache").versionAsInProject(),
        //            mavenBundle("net.sf.maltcms", "cross-events").versionAsInProject(),
        //            mavenBundle("net.sf.maltcms", "cross-tools").versionAsInProject(),
        //            mavenBundle("net.sf.maltcms", "cross-math").versionAsInProject()
        //            bundle("http://www.example.com/repository/foo-1.2.3.jar"),
        );
    }

    /**
     * Test of start method, of class CrossActivator.
     */
    @Test
    public void factoryService() throws Exception {
//        Assert.assertNotNull(factory);
    }
}
