# srcgen4j-maven-plugin
Maven plugin that runs the parse/generate workflow.

Part of the [srcgen4j](../README.md) source code generation framework — runs the parsers and generators from [srcgen4j-core](../core/) during a Maven build.

## Usage
Simply add the plugin to your project's Maven POM and add configuration and dependencies.

```xml
<plugin>
    <groupId>org.fuin.srcgen4j</groupId>
    <artifactId>srcgen4j-maven-plugin</artifactId>
    <version>0.5.0-SNAPSHOT</version>
    <configuration>
        <jaxbClasses>
            <jaxbClass>org.fuin.srcgen4j.core.velocity.VelocityGeneratorConfig</jaxbClass>
            <jaxbClass>org.fuin.srcgen4j.core.velocity.ParameterizedTemplateParserConfig</jaxbClass>
            <jaxbClass>org.fuin.srcgen4j.core.velocity.ParameterizedTemplateGeneratorConfig</jaxbClass>
        </jaxbClasses>
    </configuration>
    <executions>
        <execution>
            <id>srcgen4j</id>
            <phase>process-sources</phase>
            <goals>
                <goal>process-template</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
:warning: If you use a snapshot version, be sure to include the snapshot plugin repository to your settings (See [README](../README.md)).

## Example
You can also find an example project here: [test-project](src/test/resources-its/org/fuin/srcgen4j/maven/SrcGen4JMojoTestIT/testProcessTemplate).

## Debugging
To start the generation process in debug mode you can check out the [srcgen4j-maven-app](src/main/java/org/fuin/srcgen4j/maven/SrcGen4JMavenApp.java) class.
