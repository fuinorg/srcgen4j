# srcgen4j-commons
Common base classes for the parse/generate workflow.

Part of the [srcgen4j](../README.md) source code generation framework — it defines the configuration model and base classes the [core](../core/) and [maven-plugin](../maven/) modules build on.

The pipeline is configured using a single XML configuration file (full schema: [srcgen4j-commons-0_5_0.xsd](src/main/resources/srcgen4j-commons-0_5_0.xsd)):
```xml
<srcgen4j-config
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns="http://www.fuin.org/srcgen4j/commons/0.5.0">
    
    <variables />
    <modules />
    <parsers />
    <generators />
    
</srcgen4j-config>
```
There is currently a Maven plugin ([srcgen4j-maven](../maven/)) that executes the pipeline during a build or manually.
An Eclipse plugin is planned, but not yet available. There are some predefined parsers and generators available in the ([srcgen4j-core](../core/)) project.

## Variables
The variables section allows defining globally visible variables.
```xml
<variables>
    <variable name="path" value="/var/tmp" />
    <variable name="sub" value="${path}/mypath" />
    <variable name="escapes" value="\r\n\t" />
    <variable name="res" url="classpath:header.txt" encoding="utf-8" />
</variables>
```
A variable definition is either a simple name/value combination or an URL that points to the content.
Any content type that Java is capable to handle can be used. Additionally the content type "classpath"
allows reading files from your classpath. There is a special variable called *rootDir* that is always 
available and points to the root directory. In case of a Maven build this is the directory where you
executed the 'mvn' command.

Variables can be overwritten in the sub-sections:
```xml
<variables>
    <variable name="a" value="/var/tmp" />
</variables>
<parsers>
    <variable name="a" value="${a}/parsers1" />
</parsers>
```
The result is only visible inside the defining section.

## Modules
A module is used to define the folders where the generated output can be placed.
```xml
<modules>
  <module name="myprj" path="." maven="false">
        <folder name="doc" path="doc" create="true" override="true" clean="true" />
  </module>
</modules>
```
A Maven directory structure is assumed by default. This can be disabled with *maven="false"*.
```xml
<module name="myprj" path=".">
    <!-- It's NOT necessary to add the following! It's just to show the default folder structure. -->
    <folder name="mainJava" path="src/main/java" create="false" override="false" clean="false" />
    <folder name="mainRes" path="src/main/resources" create="false" override="false" clean="false" />
    <folder name="genMainJava" path="src-gen/main/java" clean="true" cleanExclude="\..*" />
    <folder name="genMainRes" path="src-gen/main/resources" create="true" clean="true" />
    <folder name="testJava" path="src/test/java" create="false" override="false" clean="false" />
    <folder name="testRes" path="src/test/resources" create="false" override="false" clean="false" />
    <folder name="genTestJava" path="src-gen/test/java" create="true" clean="true" />
    <folder name="genTestRes" path="src-gen/test/resources" create="true" clean="true" />
</module>
```
A folder is defined by a name that is unique within the module and a path inside the module's directory.
```xml
<folder name="mainJava" 
        path="src/main/java" 
        create="false"
        override="false" 
        overrideExclude="[A\.java|B\.java]"
        clean="false"
        cleanExclude="\..*" />
```
Other attributes that influence the generation process are:
* *create* Create the directory if it does not exist
* *override* Override existing files in that directory
* *overrideExclude* Regular expression to exclude some files from being overridden.
* *clean* All files in the directory will be deleted before new ones are created.
* *cleanExclude* Regular expression to exclude some files from being deleted.

## Parsers
The parsers section defines one or more parsers that are used to create the input models.
```xml
<parsers>
    <parser name="dddParser" class="org.fuin.srcgen4j.core.xtext.XtextParser">
        <config>
            <xtext:xtext-parser-config modelPath="src/main/domain" modelExt="ddd"
              setupClass="org.fuin.dsl.ddd.DomainDrivenDesignDslStandaloneSetup" />
        </config>
    </parser>
</parsers>
```
Every parser has a unique name and a full qualified class name that is used to instantiate the parser (using the default constructor).
Some parsers might require a special configuration that can be added in the config section.

## Generators
The generator section defines one or more generators that use the input of a parser and write their output to one of the modules.
```xml
<generators>
    <generator name="dddGenerator" class="org.fuin.srcgen4j.core.emf.EMFGenerator" parser="dddParser" module="current">
        <config>
            <!-- Generator specific configuration -->
            <emf:artifact-factory artifact="AbstractAggregate" class="org.fuin.dsl.ddd.gen.aggregate.AbstractAggregateArtifactFactory" />
        </config>
        <artifact name="AbstractAggregate" folder="genMainJava" />
        <!-- More artifact definition -->
    </generator>
</generators>
```
Every generator has a unique name and a full qualified class name that is used to instantiate the generator (using the default constructor).
Some generators might require a special configuration that can be added in the config section.
A generator creates one or more artifacts that are written to the configured folder of the module.
