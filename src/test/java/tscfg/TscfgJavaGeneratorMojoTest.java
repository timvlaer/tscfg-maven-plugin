package tscfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static tscfg.TscfgJavaGeneratorMojo.UTF_8;

@RunWith(MockitoJUnitRunner.class)
public class TscfgJavaGeneratorMojoTest {

  private TscfgJavaGeneratorMojo mojo = new TscfgJavaGeneratorMojo();

  @Rule
  public TemporaryFolder templateFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();

  @Mock
  private MavenProject project;

  @Before
  public void setUp() throws Exception {
    mojo.setProject(project);

    File templateFile = templateFolder.newFile("test.spec.conf");
    Files.write(templateFile.toPath(), templateContent(), CREATE, WRITE, TRUNCATE_EXISTING);
    mojo.setTemplateFile(templateFile);

    mojo.setOutputDirectory(outputFolder.getRoot().getAbsolutePath());
    mojo.setClassName("TestConfig");
    mojo.setPackageName("com.test.config");
  }

  @Test
  public void execute() throws Exception {
    mojo.execute();

    Mockito.verify(project).addCompileSourceRoot(outputFolder.getRoot().getAbsolutePath());

    File resultFile = new File(outputFolder.getRoot(), "com/test/config/TestConfig.java");
    assertThat(resultFile).exists();

    String result = new String(Files.readAllBytes(resultFile.toPath()), UTF_8);
    assertThat(result).contains("package com.test.config;");
    assertThat(result).contains("public class TestConfig {");
  }

  @Test
  public void executeFailsIfTemplateFileDoesNotExist() throws MojoExecutionException {
    mojo.setTemplateFile(new File(templateFolder.getRoot(), "unexisting.conf"));
    MojoExecutionException e = assertThrows(MojoExecutionException.class, mojo::execute);
    assertTrue(e.getMessage().contains("Failed to read template file"));
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

    File resultFile = new File(outputFolder.getRoot(), "com/test/config/TestConfig.java");
    Files.write(resultFile.toPath(), "extra".getBytes(UTF_8), StandardOpenOption.APPEND);

    mojo.execute();

    String result = new String(Files.readAllBytes(resultFile.toPath()), UTF_8);
    assertThat(result).doesNotContain("extra");
  }

  @Test
  public void executeFailsIfGeneratedCodeCannotBeWritten() throws Exception {
    mojo.execute();

    File resultFile = new File(outputFolder.getRoot(), "com/test/config/TestConfig.java");
    assertTrue(resultFile.setWritable(false));
    MojoExecutionException e = assertThrows(MojoExecutionException.class, mojo::execute);
    assertTrue(e.getMessage().contains("Failed to write file"));
  }

  @Test
  public void templateFileDoesNotExists() throws Exception {
    mojo.setTemplateFile(new File(outputFolder.getRoot(), "unexisting.spec.conf"));
    MojoExecutionException e = assertThrows(MojoExecutionException.class, mojo::execute);
    assertTrue(e.getMessage().contains("Failed to read template file ("));
    assertTrue(e.getMessage().contains("unexisting.spec.conf):"));
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

    File resultFile = new File(outputFolder.getRoot(), "com/test/config/TestConfig.java");
    assertThat(resultFile).exists();

    return new String(Files.readAllBytes(resultFile.toPath()), UTF_8);
  }

}
