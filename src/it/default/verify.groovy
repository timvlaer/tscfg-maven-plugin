File file = new File((File)basedir, "target/generated-sources/tscfg/com/github/timvlaer/generated/config/TestConfig.java")
if (!file.isFile()) {
    throw new FileNotFoundException("Generated class not found: " + file)
}

/*
TODO tim, try to validate the generated class:
File classpath = new File("target/its/default/target/classes");
ClassLoader cl = new URLClassLoader([classpath.toURI().toURL()] as URL[]);

Class cls = cl.loadClass("com.github.timvlaer.generated.config.TestConfig");
Constructor constructor = cls.getConstructors()[0];
Object config = constructor.newInstance(com.typesafe.config.ConfigFactory.load("tscfg/test.conf"))
assertThat(config).isNotNull()

Method getTestMethod = config.getClass().getMethod("getTest");
Object testObject = getTestMethod.invoke(config);
assertThat(testObject).isNotNull();

assertThat(invoke(testObject, "getProperty1")).isEqualTo("a");
assertThat(invoke(testObject, "getProperty2")).isEqualTo(java.util.Optional.empty());
assertThat(invoke(testObject, "getProperty3")).isEqualTo(3);
assertThat(invoke(testObject, "getProperty4")).isEqualTo(Duration.of(10, ChronoUnit.MINUTES));*/
