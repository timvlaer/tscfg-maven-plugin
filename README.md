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
    <version>0.5.0</version>
    <configuration>
        <templateFile>config-spec/service.spec.conf</templateFile>
        <packageName>com.sentiance.service.config</packageName>
        <className>ServiceConfig</className>
        <generateGetters>true</generateGetters>
        <useOptionals>false</useOptionals>
        <useDurations>false</useDurations>
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
    <version>1.3.3</version>
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

## Current limitations
* Currently only Java class generated is supported. It should be easy to extend this plugin to generate Scala files.
* This plugin always generates java classes for Java 8 and above. This should also be easy to extend.   

## Build the plugin yourself
* `git clone https://github.com/timvlaer/tscfg-maven-plugin.git`
* `cd tscfg-maven-plugin`
* `git checkout develop`
* `mvn clean install`

Configure your pom to depend on version `0.6.0-SNAPSHOT` of the plugin.
