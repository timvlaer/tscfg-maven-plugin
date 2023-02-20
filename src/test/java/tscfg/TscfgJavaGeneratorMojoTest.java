package tscfg;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static org.assertj.core.api.Assertions.*;
import static org.codehaus.plexus.logging.Logger.LEVEL_DISABLED;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TscfgJavaGeneratorMojoTest {

  private TscfgJavaGeneratorMojo mojo = new TscfgJavaGeneratorMojo();
  private Log log = new DefaultLog(new ConsoleLogger(LEVEL_DISABLED, null));

  @TempDir
  public Path templateFolder;
  @TempDir
  public Path outputFolder;

  @Mock
  private MavenProject project;

  @BeforeEach
  public void setUp() throws Exception {
    mojo.setProject(project);
    mojo.setLog(log);

    Path templateFile = templateFolder.resolve("test.spec.conf");
    Files.write(templateFile, templateContent(), CREATE, WRITE, TRUNCATE_EXISTING);
    mojo.setTemplateFile(templateFile.toFile());

    mojo.setOutputDirectory(outputFolder.toFile());
    mojo.setClassName("TestConfig");
    mojo.setPackageName("com.test.config");
  }

  @Test
  public void execute() throws Exception {
    mojo.execute();

    verify(project).addCompileSourceRoot(outputFolder.toString());

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile).exists();

    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .contains("package com.test.config;")
        .contains("public class TestConfig {");
  }

  @Test
  public void skip() throws Exception {
    mojo.setSkip(true);
    mojo.execute();

    verify(project, never()).addCompileSourceRoot(any());

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile).doesNotExist();
  }

  @Test
  public void executeFailsIfTemplateFileDoesNotExist() throws MojoExecutionException {
    mojo.setTemplateFile(templateFolder.resolve("unexisting.conf").toFile());
    assertThatExceptionOfType(MojoExecutionException.class)
        .isThrownBy(mojo::execute)
        .withMessageContaining("Failed to read template file");
  }

  @Test
  public void generateGetters() throws Exception {
    mojo.setGenerateGetters(true);

    Path resultFile = executeMojo();
    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .contains("int getPort()")
        .contains("String getServer()")
        .contains("long getLength()");
  }

  @Test
  public void generateRecords() throws Exception {
    mojo.setGenerateRecords(true);

    Path resultFile = executeMojo();
    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .contains("public record TestConfig(")
        .contains("public static record Test(");
  }

  @Test
  public void useOptionals() throws Exception {
    mojo.setUseOptionals(true);

    Path resultFile = executeMojo();
    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .contains("java.util.Optional<java.lang.String> server");
  }

  @Test
  public void useDurations() throws Exception {
    mojo.setUseDurations(true);

    Path resultFile = executeMojo();
    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .contains("public final java.time.Duration length;");
  }

  @Test
  public void executeFullyOverwritesGeneratedFile() throws Exception {
    mojo.execute();

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    Files.write(resultFile, "extra".getBytes(UTF_8), APPEND);

    mojo.execute();

    assertThat(contentOf(resultFile.toFile(), UTF_8))
        .doesNotContain("extra");
  }

  @Test
  public void executeFailsIfGeneratedCodeCannotBeWritten() throws Exception {
    mojo.execute();

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile.toFile().setWritable(false)).isTrue();
    assertThatExceptionOfType(MojoExecutionException.class)
        .isThrownBy(mojo::execute)
        .withMessageContaining("Failed to write file");
  }

  @Test
  public void templateFileDoesNotExists() throws Exception {
    mojo.setTemplateFile(outputFolder.resolve("unexisting.spec.conf").toFile());
    assertThatExceptionOfType(MojoExecutionException.class)
        .isThrownBy(mojo::execute)
        .withMessageContaining("Failed to read template file (")
        .withMessageContaining("unexisting.spec.conf):");
  }

  private byte[] templateContent() {
    String templateConfig = "test {\n" +
        "  server: \"string?\"\n" +
        "  port: \"int\"\n" +
        "  length: \"duration\"\n" +
        "}";
    return templateConfig.getBytes(UTF_8);
  }

  private Path executeMojo() throws MojoExecutionException, IOException {
    mojo.execute();

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile).exists();

    return resultFile;
  }

}
