package tscfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;
import static org.assertj.core.api.Assertions.*;
import static tscfg.TscfgJavaGeneratorMojo.UTF_8;

@ExtendWith(MockitoExtension.class)
public class TscfgJavaGeneratorMojoTest {

  private TscfgJavaGeneratorMojo mojo = new TscfgJavaGeneratorMojo();

  @TempDir
  public Path templateFolder;
  @TempDir
  public Path outputFolder;

  @Mock
  private MavenProject project;

  @BeforeEach
  public void setUp() throws Exception {
    mojo.setProject(project);

    Path templateFile = templateFolder.resolve("test.spec.conf");
    Files.write(templateFile, templateContent(), CREATE, WRITE, TRUNCATE_EXISTING);
    mojo.setTemplateFile(templateFile.toFile());

    mojo.setOutputDirectory(outputFolder.toString());
    mojo.setClassName("TestConfig");
    mojo.setPackageName("com.test.config");
  }

  @Test
  public void execute() throws Exception {
    mojo.execute();

    Mockito.verify(project).addCompileSourceRoot(outputFolder.toString());

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile).exists();

    String result = new String(Files.readAllBytes(resultFile), UTF_8);
    assertThat(result).contains("package com.test.config;");
    assertThat(result).contains("public class TestConfig {");
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

    String result = executeMojo();
    assertThat(result).contains("int getPort()");
    assertThat(result).contains("String getServer()");
    assertThat(result).contains("long getLength()");
  }

  @Test
  public void generateRecords() throws Exception {
    mojo.setGenerateRecords(true);

    String result = executeMojo();
    assertThat(result).contains("public record TestConfig(");
    assertThat(result).contains("public static record Test(");
  }

  @Test
  public void useOptionals() throws Exception {
    mojo.setUseOptionals(true);

    String result = executeMojo();
    assertThat(result).contains("java.util.Optional<java.lang.String> server");
  }

  @Test
  public void useDurations() throws Exception {
    mojo.setUseDurations(true);

    String result = executeMojo();
    assertThat(result).contains("public final java.time.Duration length;");
  }

  @Test
  public void executeFullyOverwritesGeneratedFile() throws Exception {
    mojo.execute();

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    Files.write(resultFile, "extra".getBytes(UTF_8), APPEND);

    mojo.execute();

    String result = new String(Files.readAllBytes(resultFile), UTF_8);
    assertThat(result).doesNotContain("extra");
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

  private String executeMojo() throws MojoExecutionException, IOException {
    mojo.execute();

    Path resultFile = outputFolder.resolve("com").resolve("test").resolve("config").resolve("TestConfig.java");
    assertThat(resultFile).exists();

    return new String(Files.readAllBytes(resultFile), UTF_8);
  }

}
