[![Build Status](https://www.travis-ci.org/timvlaer/tscfg-maven-plugin.svg?branch=master)](https://www.travis-ci.org/timvlaer/tscfg-maven-plugin)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timvlaer/tscfg-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timvlaer/tscfg-maven-plugin)

# tscfg-maven-plugin

Maven plugin that generates the boiler-plate for a [Typesafe Config](https://github.com/typesafehub/config) properties file
using the excellent [tscfg](https://github.com/carueda/tscfg) library by @carueda.

This plugin takes a configuration file (templateFile) and generates objects to hold your configuration.

## Usage
```xml
<plugin>
    <groupId>com.github.timvlaer</groupId>
    <artifactId>tscfg-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <templateFile>config-spec/service.spec.conf</templateFile>
        <packageName>com.sentiance.service.config</packageName>
        <className>ServiceConfig</className>
        <generateGetters>true</generateGetters>
        <useOptionals>false</useOptionals>
        <useDurations>true</useDurations>
    </configuration>
     <executions>
        <execution>
            <id>tscfg-sources</id>
            <goals>
                <goal>generate-config-class</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

To compile the generated code, add the [Lightbend Config](https://github.com/lightbend/config) dependency to your project:
```xml
<dependency>
    <groupId>com.typesafe</groupId>
    <artifactId>config</artifactId>
    <version>1.3.4</version>
</dependency>
```

## Configuration
* templateFile: typesafe configuration template file
* className: the name of the generated config class 
* packageName: the package of the generated config class
* outputDirectory: the output directory for the generated class, default is `target/generated-sources/tscfg/`
* generateGetters: (true|false) generate getters for configuration
* useOptionals: (true|false) use java8 Optional object for optional fields
* useDurations: (true|false) if true, properties of type `duration` will be of type `java.time.Duration` in the generated code. If false, it will be a long.
* allRequired: (true|false) if true, optional tags are ignored and every property in the config file is required. Defaults to false.

## Current limitations
* This plugin currently only generates Java code.
* This plugin always generates java classes for Java 8 and above.
Open a ticket or PR if you'd like to change the above.

## Build the plugin yourself
* `git clone https://github.com/timvlaer/tscfg-maven-plugin.git`
* `cd tscfg-maven-plugin`
* Update the `<version>` in the pom.xml file, e.g. `2.0.0-custom-SNAPSHOT` 
* `mvn clean install`
* Configure your project's pom to depend on your version of the plugin (e.g. `2.0.0-custom-SNAPSHOT`.)
