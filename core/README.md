# srcgen4j-core
Parsers and generators (EMF, Velocity, Xtext).

Part of the [srcgen4j](../README.md) source code generation framework — ready-to-use parsers and generators built on [srcgen4j-commons](../commons/).

* *XtextParser* Parses [Xtext](https://eclipse.dev/Xtext/) models.
* *EMFGenerator* Generates content based on an ECORE ResourceSet (for example created with [Xtext](https://eclipse.dev/Xtext/)). 
* *ParameterizedTemplateParser* Parses a given directory for XML files of type [ParameterizedTemplateModel](src/main/java/org/fuin/srcgen4j/core/velocity/ParameterizedTemplateModel.java) or [ParameterizedTemplateModels](src/main/java/org/fuin/srcgen4j/core/velocity/ParameterizedTemplateModels.java) and combines all files into one model.
* *ParameterizedTemplateGenerator* Generates files for a model from the *ParameterizedTemplateParser* using the [Velocity template engine](http://velocity.apache.org/).

## Configuration schemas
Each technology has its own XSD for the `<config>` section of a parser or generator:
- [srcgen4j-core-base-0_5_0.xsd](src/main/resources/srcgen4j-core-base-0_5_0.xsd) — common base types
- [srcgen4j-core-emf-0_5_0.xsd](src/main/resources/srcgen4j-core-emf-0_5_0.xsd) — EMF generator config
- [srcgen4j-core-velocity-0_5_0.xsd](src/main/resources/srcgen4j-core-velocity-0_5_0.xsd) — Velocity parser/generator config
- [srcgen4j-core-xtext-0_5_0.xsd](src/main/resources/srcgen4j-core-xtext-0_5_0.xsd) — Xtext parser config

## XtextParser
The parser is configured with the path where the model files with a dedicated extension can be found. 
The setup class attribute is used to instantiate the [Xtext](https://eclipse.dev/Xtext/) parser itself.
```xml
<parser name="ptp" class="org.fuin.srcgen4j.core.xtext.XtextParser">
    <config>
        <xtext:xtext-parser-config modelPath="${testRes}" modelExt="xsdsl"
               setupClass="org.fuin.xsample.XSampleDslStandaloneSetup" />
    </config>
</parser>
```

A full blown example for the Xtext based [DDD DSL](https://github.com/fuinorg/ddd-cqrs-dsl) can be found [here](https://github.com/fuinorg/org.fuin.dsl.ddd/tree/master/ddd-dsl-test). 

## EMFGenerator
The EMF generator requires setting up the different [artifact factories](../commons/src/main/java/org/fuin/srcgen4j/commons/ArtifactFactory.java) that generate code for different EMF model elements.
```xml
<generator name="gen1" class="org.fuin.srcgen4j.core.emf.EMFGenerator" parser="ptp" module="current">
    <config>
        <emf:emf-generator-config>
            <emf:artifact-factory artifact="abstractHello" class="org.fuin.srcgen4j.core.emf.AbstractHelloTstGen">
                <variable name="package" value="a.b.c" />
            </emf:artifact-factory>
        </emf:emf-generator-config>
    </config>
    <artifact name="abstractHello" folder="testGenMainJava" />
</generator>
```
You can also define local variables that will be provided to the artifact factory. 

## ParameterizedTemplateParser
The parser is configured with the path where the model files can be found.  
```xml
<parser name="ptp" class="org.fuin.srcgen4j.core.velocity.ParameterizedTemplateParser">
    <config>
        <velo:parameterized-template-parser modelPath="${testRes}" 
                                            modelFilter=".*\.ptg\.xml"
                                            templatePath="${testRes}" 
                                            templateFilter=".*\.ptg\.java" />
    </config>
</parser>
```
A model element always consists of two parts: An XML definition and a velocity template for code generation.

An example template definition ([parameterized-template-1.ptg.xml](src/test/resources/parameterized-template-1.ptg.xml)):
```xml
<parameterized-template template="parameterized-template-1.ptg.java" xmlns="http://www.fuin.org/srcgen4j/core/velocity">
    
    <!-- Variables that can be used in the velocity template -->
    
    <arguments>
        <argument key="name" value="-" />
        <argument key="pkg" value="-" />
    </arguments>

    <!-- Files to be generated with constant values for the above defined variables -->
        
    <target-file path="a" name="A.java">
        <argument key="name" value="A" />
        <argument key="pkg" value="a" />
    </target-file>
    
    <target-file path="b" name="B.java">
        <argument key="name" value="B" />
        <argument key="pkg" value="b" />
    </target-file>
    
</parameterized-template>
```

An example velocity template ([parameterized-template-1.ptg.java](src/test/resources/parameterized-template-1.ptg.java)):
```java
package ${pkg};

public class ${name} {
    // Whatever
}
```

It's also possible to create the variable values programmatically (See [TestTFLProducer](src/test/java/org/fuin/srcgen4j/core/velocity/TestTFLProducer.java)):
```xml
<parameterized-template template="parameterized-template-2.ptg.java" xmlns="http://www.fuin.org/srcgen4j/core/velocity">
    
    <arguments>
        <argument key="name" value="-" />
        <argument key="pkg" value="-" />
    </arguments>

    <target-file-list-producer class="org.fuin.srcgen4j.core.velocity.TestTFLProducer" />
    
</parameterized-template>
```

## ParameterizedTemplateGenerator
The generator is simply configured with the path to the velocity templates (See topic Resource Management / [file.resource.loader.path](http://velocity.apache.org/engine/2.0/configuration.html)).
```xml
<generator name="gen1" class="org.fuin.srcgen4j.core.velocity.ParameterizedTemplateGenerator" 
           parser="ptp" module="current" folder="testJava">
    <config>
        <velo:parameterized-template-generator templatePath="${testRes}" />
    </config>
    <artifact name="file" />
</generator>
```