import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.contentOf

def generatedClass = new File((File) basedir, "target/generated-sources/tscfg/com/github/timvlaer/generated/config/TestConfig.java")
assertThat(generatedClass).exists()

assertThat(contentOf(generatedClass))
        .contains("String getProperty1()")
        .contains("Optional<java.lang.String> getProperty2()")
        .contains("int getProperty3()")
        .contains("Duration getProperty4()")

return true
