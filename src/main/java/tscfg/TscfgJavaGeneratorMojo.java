package tscfg;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tscfg.generators.GenOpts;
import tscfg.generators.GenResult;
import tscfg.generators.Generator;
import tscfg.generators.java.JavaGen;
import tscfg.ns.NamespaceMan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

@Mojo(name = "generate-config-class", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES, goal = "generate-config-class")
public class TscfgJavaGeneratorMojo extends AbstractMojo {

  private static final String PACKAGE_SEPARATOR = ".";
  private static final String JAVA_FILE_EXTENSION = ".java";

  @Parameter(required = true)
  private File templateFile;

  @Parameter(required = true)
  private String packageName;

  @Parameter(required = true)
  private String className;

  @Parameter(defaultValue = "${project.build.directory}/generated-sources/tscfg/")
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "false")
  private boolean generateGetters;

  @Parameter(defaultValue = "false")
  private boolean generateRecords;

  @Parameter(defaultValue = "false")
  private boolean useOptionals;

  @Parameter(defaultValue = "true")
  private boolean useDurations;

  @Parameter(defaultValue = "false")
  private boolean allRequired;

  @Override
  public void execute() throws MojoExecutionException {
    GenResult generatorResult = generateJavaCodeForTemplate(templateFile);
    writeGeneratedCodeToJavaFile(generatorResult.code());

    getLog().debug("Adding " + outputDirectory + " as source root in the maven project.");
    project.addCompileSourceRoot(outputDirectory);
  }

  private GenResult generateJavaCodeForTemplate(File templateFile) throws MojoExecutionException {
    boolean useBackticks = false;
    boolean generateJava7Code = false;
    boolean generateScala12Code = true;
    GenOpts genOpts = new GenOpts(
        packageName,
        className,
        allRequired,
        generateJava7Code,
        generateScala12Code,
        useBackticks,
        generateGetters,
        generateRecords,
        useOptionals,
        useDurations
    );
    NamespaceMan rootNamespace = new NamespaceMan();
    Generator tscfgGenerator = new JavaGen(genOpts, rootNamespace);
    return tscfgGenerator.generate(ModelBuilder.apply(rootNamespace, readTscfgTemplate(templateFile), allRequired).objectType());
  }

  private String readTscfgTemplate(File templateFile) throws MojoExecutionException {
    try {
      return new String(Files.readAllBytes(templateFile.toPath()), UTF_8);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to read template file (" + templateFile + "): " + e.getClass() + ": " + e.getMessage());
    }
  }

  private void writeGeneratedCodeToJavaFile(String javaClassCode) throws MojoExecutionException {
    Path javaClassFile = assembleJavaFilePath();
    try {
      createParentDirsIfNecessary(javaClassFile);
      Files.write(javaClassFile, javaClassCode.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
      getLog().debug("Wrote generated java config file to " + javaClassFile);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write file (" + javaClassFile + "). " +
          e.getClass() + ": " + e.getMessage());
    }
  }

  private Path assembleJavaFilePath() {
    String packageDirectoryPath = packageName.replace(PACKAGE_SEPARATOR, File.separator);
    String javaFileName = className + JAVA_FILE_EXTENSION;
    return Paths.get(outputDirectory, packageDirectoryPath, javaFileName);
  }

  private void createParentDirsIfNecessary(Path outputFile) throws IOException {
    Path parentDir = outputFile.getParent();
    if (!Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
  }

  void setTemplateFile(File templateFile) {
    this.templateFile = templateFile;
  }

  void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  void setClassName(String className) {
    this.className = className;
  }

  void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  void setProject(MavenProject project) {
    this.project = project;
  }

  void setGenerateGetters(boolean generateGetters) {
    this.generateGetters = generateGetters;
  }

  void setGenerateRecords(boolean generateRecords) {
    this.generateRecords = generateRecords;
  }

  void setUseOptionals(boolean useOptionals) {
    this.useOptionals = useOptionals;
  }

  void setUseDurations(boolean useDurations) {
    this.useDurations = useDurations;
  }
}
