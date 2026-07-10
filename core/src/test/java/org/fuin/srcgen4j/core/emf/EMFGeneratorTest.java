/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.srcgen4j.core.emf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import jakarta.xml.bind.JAXBContext;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fuin.srcgen4j.commons.DefaultContext;
import org.fuin.srcgen4j.commons.GeneratorConfig;
import org.fuin.srcgen4j.commons.ParseException;
import org.fuin.srcgen4j.commons.ParserConfig;
import org.fuin.srcgen4j.commons.SrcGen4JConfig;
import org.fuin.srcgen4j.core.SchemaHelper;
import org.fuin.srcgen4j.core.xtext.XtextParser;
import org.fuin.srcgen4j.core.xtext.XtextParserConfig;
import org.fuin.utils4j.classpath.Handler;
import org.fuin.utils4j.jaxb.JaxbUtils;
import org.fuin.utils4j.jaxb.UnmarshallerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link XtextParser}.
 */
class EMFGeneratorTest {

    private static DefaultContext CONTEXT;
    private static File RESOURCES_DIR;


    @BeforeAll
    static void  beforeAll() {
        Handler.add();
        CONTEXT = new DefaultContext();
        RESOURCES_DIR = new File("src/test/resources");
    }

    @Test
    void testParse() throws Exception {

        final File file = new File(new File(RESOURCES_DIR, "domain"), "xtext-test-config.xml");

        final Unmarshaller unmarshaller = createUnmarshaller();
        final SrcGen4JConfig srcGen4JConfig = JaxbUtils.unmarshal(unmarshaller, file);
        srcGen4JConfig.init(CONTEXT, new File("."));
        final GeneratorConfig generatorConfig = srcGen4JConfig.getGenerators().findByName("gen1");
        final ParserConfig parserConfig = srcGen4JConfig.getParsers().getList().get(0);

        final XtextParser parser = new XtextParser();
        parser.initialize(CONTEXT, parserConfig);
        final ResourceSet resourceSet = parser.parse();

        // The parser must mark the explicitly loaded model resources as "primary"
        assertThat(resourceSet.getResources()).isNotEmpty();
        for (final org.eclipse.emf.ecore.resource.Resource resource : resourceSet.getResources()) {
            assertThat(PrimaryResources.isPrimary(resource)).isTrue();
        }

        final EMFGenerator testee = new EMFGenerator();
        testee.initialize(generatorConfig);

        // TEST
        testee.generate(resourceSet, false);

        // VERIFY

        assertThat(new File("target/xtest-test/a/b/c/AbstractHelloUniverse.java"))
                .hasSameTextualContentAs(new File("src/test/resources/AbstractHelloUniverse.java"));
        assertThat(new File("target/xtest-test/a/b/c/HelloUniverse.java"))
                .hasSameTextualContentAs(new File("src/test/resources/HelloUniverse.java"));

        assertThat(new File("target/xtest-test/a/b/c/AbstractHelloWorld.java"))
                .hasSameTextualContentAs(new File("src/test/resources/AbstractHelloWorld.java"));
        assertThat(new File("target/xtest-test/a/b/c/HelloWorld.java"))
                .hasSameTextualContentAs(new File("src/test/resources/HelloWorld.java"));

        assertThat(new File("target/xtest-test/a/b/c/AbstractHelloResource.java"))
                .hasSameTextualContentAs(new File("src/test/resources/AbstractHelloResource.java"));
        assertThat(new File("target/xtest-test/a/b/c/HelloResource.java"))
                .hasSameTextualContentAs(new File("src/test/resources/HelloResource.java"));

    }

    @Test
    void testParseError() throws Exception {

        final String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <srcgen4j-config xmlns="http://www.fuin.org/srcgen4j/commons/0.5.0"
                                 xmlns:xtext="http://www.fuin.org/srcgen4j/core/xtext/0.5.0"
                                 xmlns:emf="http://www.fuin.org/srcgen4j/core/emf/0.5.0">
                
                	<modules>
                		<module name="current" path="." maven="false">
                			<folder name="testGenMainJava" path="target/xtest-test" create="true" override="true" clean="false" />
                			<folder name="testMainJava" path="target/xtest-test" create="true" override="true" clean="false" />
                			<folder name="testGenMainRes" path="target/xtest-test" create="true" override="true" clean="false" />
                		</module>
                	</modules>
                
                	<parsers>
                		<parser name="ptp" class="org.fuin.srcgen4j.core.xtext.XtextParser">
                			<config>
                				<xtext:xtext-parser-config modelPath="classpath:/xtext-error.xsdsl" modelExt="xsdsl"
                				                    setupClass="org.fuin.xsample.XSampleDslStandaloneSetup" />
                			</config>
                		</parser>
                	</parsers>
                
                	<generators>
                		<generator name="gen1" class="org.fuin.srcgen4j.core.emf.EMFGenerator" parser="ptp">
                		    <config>
                				<emf:emf-generator-config>
                					<emf:artifact-factory artifact="abstractHello" class="org.fuin.srcgen4j.core.emf.AbstractHelloTstGen"
                					                      module="current" folder="testGenMainJava">
                						<variable name="package" value="a.b.c" />
                					</emf:artifact-factory>
                					<emf:artifact-factory artifact="manualHello" class="org.fuin.srcgen4j.core.emf.ManualHelloTstGen"
                					                      module="current" folder="testMainJava" />
                					<emf:artifact-factory artifact="helloProps" incremental="false" class="org.fuin.srcgen4j.core.emf.HelloPropertiesTstGen"
                					                      module="current" folder="testGenMainRes" />
                				</emf:emf-generator-config>
                			</config>
                		</generator>
                	</generators>
                
                </srcgen4j-config>                
                """;

        final Unmarshaller unmarshaller = createUnmarshaller();
        final SrcGen4JConfig srcGen4JConfig = JaxbUtils .unmarshal(unmarshaller, xml);
        srcGen4JConfig.init(CONTEXT, new File("."));
        final ParserConfig parserConfig = srcGen4JConfig.getParsers().getList().get(0);

        final XtextParser parser = new XtextParser();
        parser.initialize(CONTEXT, parserConfig);
        try {
            parser.parse();
            fail();
        } catch (final ParseException ex) {
            // OK
        }

    }

    private static Unmarshaller createUnmarshaller() throws JAXBException {
        final Unmarshaller unmarshaller = new UnmarshallerBuilder()
                .withContext(JAXBContext.newInstance(SrcGen4JConfig.class, XtextParserConfig.class, EMFGeneratorConfig.class)).build();
        // Validate against the XSDs. The schema is built with a fixed source order (commons first) because the emf schema
        // imports the commons schema - see SchemaHelper for details.
        unmarshaller.setSchema(SchemaHelper.createCoreSchema());
        return unmarshaller;
    }

}
