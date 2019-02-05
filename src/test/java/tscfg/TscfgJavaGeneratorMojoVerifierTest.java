package tscfg;

import com.typesafe.config.ConfigFactory;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TscfgJavaGeneratorMojoVerifierTest {

    @Test
    public void executeGenerateConfigClass() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/integration-test");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteDirectory("target");
        verifier.deleteArtifact("com.github.timvlaer", "tscfg-maven-plugin-test", "0.0.0", "jar");

        verifier.executeGoal("compile");

        String expectedGeneratedConfigFile = "target/generated-sources/tscfg/com/github/timvlaer/generated/config/TestConfig.java";
        verifier.assertFilePresent(expectedGeneratedConfigFile);

        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        verifyGeneratedConfigurationObject();

        verifier.deleteDirectory("target");
    }

    private void verifyGeneratedConfigurationObject() throws Exception {
        File classpath = new File("target/test-classes/integration-test/target/classes");
        ClassLoader cl = new URLClassLoader(new URL[]{classpath.toURI().toURL()});

        Class cls = cl.loadClass("com.github.timvlaer.generated.config.TestConfig");
        Constructor constructor = cls.getConstructors()[0];
        Object config = constructor.newInstance(ConfigFactory.load("integration-test/tscfg/test.conf"));
        assertThat(config).isNotNull();

        Method getTestMethod = config.getClass().getMethod("getTest");
        Object testObject = getTestMethod.invoke(config);
        assertThat(testObject).isNotNull();

        assertThat(invoke(testObject, "getProperty1")).isEqualTo("a");
        assertThat(invoke(testObject, "getProperty2")).isEqualTo(Optional.empty());
        assertThat(invoke(testObject, "getProperty3")).isEqualTo(3);
        assertThat(invoke(testObject, "getProperty4")).isEqualTo(Duration.of(10, ChronoUnit.MINUTES));
    }

    private Object invoke(Object testObject, String methodName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return testObject.getClass().getMethod(methodName).invoke(testObject);
    }
}
