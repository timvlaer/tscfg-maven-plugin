[![Build Status](https://www.travis-ci.org/timvlaer/tscfg-maven-plugin.svg?branch=master)](https://www.travis-ci.org/timvlaer/tscfg-maven-plugin)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

# tscfg-maven-plugin

Maven plugin that generates the boiler-plate for a [Typesafe Config](https://github.com/typesafehub/config) properties file using the excellent [tscfg](https://github.com/carueda/tscfg) library by carueda.

This plugin takes a configuration file (templateFile) and generates objects to hold your configuration.

## Usage
```xml
<plugin>
    <groupId>com.github.timvlaer</groupId>
    <artifactId>tscfg-maven-plugin</artifactId>
    <version>0.2.0</version>
    <configuration>
        <templateFile>config-spec/aligner.spec.conf</templateFile>
        <packageName>com.sentiance.service.aligner.config</packageName>
        <className>AlignerConfig</className>
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

## Configuration
* templateFile: typesafe configuration template file
* className: the name of the generated config class 
* packageName: the package of the generated config class
* outputDirectory: the output directory for the generated class, default is `target/generated-sources/tscfg/`

## Current limitations
* Currently only Java class generated is supported. It should be easy to extend this plugin to generate Scala files.
* This plugin always generates java classes for Java 8 and above. This should also be easy to extend.   
