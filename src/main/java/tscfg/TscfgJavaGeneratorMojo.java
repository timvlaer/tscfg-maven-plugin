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

/**
 * Maven plugin that generates the boiler-plate Java code for a <a href="https://github.com/lightbend/config">Typesafe Config</a> properties file
 * using <a href="https://github.com/carueda/tscfg">tscfg</a>.
 */
@Mojo(name = "generate-config-class", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES, goal = "generate-config-class")
public class TscfgJavaGeneratorMojo extends AbstractMojo {

  private static final String PACKAGE_SEPARATOR = ".";
  private static final String JAVA_FILE_EXTENSION = ".java";

  /**
   * The Typesafe configuration template file.
   */
  @Parameter(required = true)
  private File templateFile;

  /**
   * The package of the generated config class.
   */
  @Parameter(required = true)
  private String packageName;

  /**
   * The name of the generated config class.
   */
  @Parameter(required = true)
  private String className;

  /**
   * The output directory for the generated class.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/tscfg/")
  private String outputDirectory;

  /**
   * The maven project being built.
   */
  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  /**
   * Generate getters for configuration?
   *
   * @since 0.3.0
   */
  @Parameter(defaultValue = "false")
  private boolean generateGetters;

  /**
   * Generate records for configuration?
   *
   * @since 1.0.2
   */
  @Parameter(defaultValue = "false")
  private boolean generateRecords;

  /**
   * Use {@link java.util.Optional} for optional fields?
   *
   * @since 0.6.0
   */
  @Parameter(defaultValue = "false")
  private boolean useOptionals;

  /**
   * Use {@link java.time.Duration} for duration fields?
   *
   * @since 0.6.0
   */
  @Parameter(defaultValue = "true")
  private boolean useDurations;

  /**
   * Optional tags are ignored and every property in the config file is required?
   *
   * @since 0.7.0
   */
  @Parameter(defaultValue = "false")
  private boolean allRequired;

  /**
   * Construct a new instance.
   */
  public TscfgJavaGeneratorMojo() {
    super();
  }

  /**
   * Generate the boiler-plate Java code for a <a href="https://github.com/lightbend/config">Typesafe Config</a> properties file
   * using <a href="https://github.com/carueda/tscfg">tscfg</a>.
   *
   * @throws MojoExecutionException if execution fails.
   */
  @Override
  public void execute() throws MojoExecutionException {
    GenResult generatorResult = generateJavaCodeForTemplate(templateFile);
    writeGeneratedCodeToJavaFile(generatorResult.code());

    getLog().debug("Adding " + outputDirectory + " as source root in the maven project.");
    project.addCompileSourceRoot(outputDirectory);
  }

  /**
   * Generate Java code based on the Typesafe configuration template file.
   *
   * @param templateFile the template configuration file.
   * @return the Java code generator result.
   * @throws MojoExecutionException if the Java code generation fails.
   */
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

  /**
   * Read a Typesafe configuration template file.
   *
   * @param templateFile the Typesafe configuration template file.
   * @return the Typesafe configuration template.
   * @throws MojoExecutionException if the Typesafe configuration template file could not be read.
   */
  private String readTscfgTemplate(File templateFile) throws MojoExecutionException {
    try {
      return new String(Files.readAllBytes(templateFile.toPath()), UTF_8);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to read template file (" + templateFile + "): " +
          e.getClass() + ": " + e.getMessage(), e);
    }
  }

  /**
   * Write the generated Java class code to a file.
   *
   * @param javaClassCode the generated Java code.
   * @throws MojoExecutionException if the generated Java class code could not be written to a file.
   */
  private void writeGeneratedCodeToJavaFile(String javaClassCode) throws MojoExecutionException {
    Path javaClassFile = assembleJavaFilePath();
    try {
      createParentDirsIfNecessary(javaClassFile);
      Files.write(javaClassFile, javaClassCode.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
      getLog().debug("Wrote generated java config file to " + javaClassFile);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write file (" + javaClassFile + "). " +
          e.getClass() + ": " + e.getMessage(), e);
    }
  }

  /**
   * Construct the file path for the generated Java class code.
   *
   * @return the file path for the generated Java class code.
   */
  private Path assembleJavaFilePath() {
    String packageDirectoryPath = packageName.replace(PACKAGE_SEPARATOR, File.separator);
    String javaFileName = className + JAVA_FILE_EXTENSION;
    return Paths.get(outputDirectory, packageDirectoryPath, javaFileName);
  }

  /**
   * Create parent directories if necessary for a file path.
   *
   * @param outputFile the file path.
   * @throws IOException if the parent directories could not be created.
   */
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
