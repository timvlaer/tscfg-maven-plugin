import static java.nio.charset.StandardCharsets.UTF_8
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.contentOf

def generatedClass = new File((File) basedir, "target/generated-sources/tscfg/com/github/timvlaer/generated/config/TestConfig.java")
assertThat(generatedClass).exists()

assertThat(contentOf(generatedClass, UTF_8))
        .contains("String getProperty1()")
        .contains("Optional<java.lang.String> getProperty2()")
        .contains("int getProperty3()")
        .contains("Duration getProperty4()")

def compiledClass = new File((File) basedir, "target/classes/com/github/timvlaer/generated/config/TestConfig.class")
assertThat(compiledClass).exists()

return true
