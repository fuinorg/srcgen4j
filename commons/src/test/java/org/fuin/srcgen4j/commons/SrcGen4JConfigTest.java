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
package org.fuin.srcgen4j.commons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;

import org.fuin.utils4j.jaxb.JaxbUtils;
import org.fuin.utils4j.jaxb.UnmarshallerBuilder;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;

/**
 * Tests for {@link SrcGen4JConfig}.
 */
public class SrcGen4JConfigTest extends AbstractTest {

    @Test
    public final void testPojoStructureAndBehavior() {

        final PojoClass pc = PojoClassFactory.getPojoClass(SrcGen4JConfig.class);
        final Validator validator = createPojoValidatorBuilder().build();
        validator.validate(pc);

    }

    @Test
    public final void testGetVarMap() {

        // PREPARE
        final Variables vars = new Variables(new Variable("a", "1"), new Variable("B", "b"), new Variable("x", "2"));
        final SrcGen4JConfig testee = new SrcGen4JConfig();
        testee.setVariables(vars);
        testee.init(new DefaultContext(), new File("."));

        // TEST
        final Map<String, String> varMap = testee.getVarMap();

        // VERIFY
        assertThat(varMap).isNotNull().containsOnly(entry("rootDir", "."), entry("a", "1"), entry("B", "b"), entry("x", "2"));

    }

    @Test
    public final void testUnmarshal() throws Exception {

        // PREPARE
        final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class);
        final Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test-config.xml"));
        try {

            // TEST
            final SrcGen4JConfig testee = JaxbUtils.unmarshal(new UnmarshallerBuilder().withContext(jaxbContext)
                    .addClasspathSchemas("/srcgen4j-commons-0_5_0.xsd", "/test-input.xsd").build(), reader);

            // VERIFY
            assertThat(testee).isNotNull();

            final Variables variables = testee.getVariables();
            assertThat(variables).isNotNull();
            final List<Variable> varList = variables.asList();
            assertThat(varList).hasSize(2);
            final Variable var0 = varList.get(0);
            assertThat(var0.getName()).isEqualTo("root");
            assertThat(var0.getValue()).isEqualTo("/var/tmp");
            final Variable var1 = varList.get(1);
            assertThat(var1.getName()).isEqualTo("project_example_path");
            assertThat(var1.getValue()).isEqualTo("${root}/example");

            assertThat(testee.getProjects()).isNotNull();
            assertThat(testee.getProjects()).hasSize(1);
            final Project prj = testee.getProjects().get(0);
            assertThat(prj.getName()).isEqualTo("example");
            assertThat(prj.getPath()).isEqualTo("${root}/example");
            assertThat(prj.getFolders()).hasSize(8);
            assertThat(prj.getFolders()).contains(new Folder("mainJava", ""), new Folder("mainRes", ""), new Folder("genMainJava", ""),
                    new Folder("genMainRes", ""), new Folder("testJava", ""), new Folder("testRes", ""), new Folder("genTestJava", ""),
                    new Folder("genTestRes", ""));
            final int idxGenMainJava = prj.getFolders().indexOf(new Folder("genMainJava", ""));
            assertThat(idxGenMainJava).isNotNegative();
            assertThat(prj.getFolders().get(idxGenMainJava).getCleanExclude()).isEqualTo("\\..*");

            final List<Replacer> replacers = testee.getReplacers();
            assertThat(replacers).isNotNull();
            assertThat(replacers).hasSize(2);
            final Replacer replacer0 = replacers.get(0);
            assertThat(replacer0.getName()).isEqualTo("pkg");
            assertThat(replacer0.getExtension()).isNull();
            assertThat(replacer0.getExpression()).isEqualTo("org\\.old\\.(.*)");
            assertThat(replacer0.getReplacement()).isEqualTo("org.new.$1");
            final Replacer replacer1 = replacers.get(1);
            assertThat(replacer1.getName()).isEqualTo("header");
            assertThat(replacer1.getExtension()).isEqualTo("java");
            assertThat(replacer1.getExpression()).isEqualTo("(.*)");
            assertThat(replacer1.getReplacement()).isEqualTo("X$1");

            assertThat(testee.getGenerators()).isNotNull();
            assertThat(testee.getGenerators().getList()).hasSize(1);
            final GeneratorConfig gen = testee.getGenerators().getList().get(0);
            assertThat(gen.getName()).isEqualTo("gen1");
            assertThat(gen.getClassName()).isEqualTo("org.fuin.srcgen4j.commons.TestGenerator");
            assertThat(gen.getParser()).isEqualTo("parser1");

        } finally {
            reader.close();
        }
    }

    @Test
    public final void testMarshalReplacers() throws Exception {

        // PREPARE
        final SrcGen4JConfig testee = new SrcGen4JConfig();
        final List<Replacer> replacers = new ArrayList<>();
        replacers.add(new Replacer("pkg", "org\\.old\\.(.*)", "org.new.$1"));
        replacers.add(new Replacer("header", "java", "(.*)", "X$1"));
        testee.setReplacers(replacers);

        // TEST
        final String result = JaxbUtils.marshal(testee, SrcGen4JConfig.class);

        // VERIFY the list is wrapped in "replacers" and each element is named "replacer"
        final Map<String, String> ns = Map.of("sg", NS_SG4JC);
        XmlAssert.assertThat(result).withNamespaceContext(ns)
                .nodesByXPath("/sg:srcgen4j-config/sg:replacers/sg:replacer").hasSize(2);
        XmlAssert.assertThat(result).withNamespaceContext(ns)
                .valueByXPath("/sg:srcgen4j-config/sg:replacers/sg:replacer[1]/@name").isEqualTo("pkg");
        XmlAssert.assertThat(result).withNamespaceContext(ns)
                .valueByXPath("/sg:srcgen4j-config/sg:replacers/sg:replacer[1]/@expression").isEqualTo("org\\.old\\.(.*)");
        XmlAssert.assertThat(result).withNamespaceContext(ns)
                .valueByXPath("/sg:srcgen4j-config/sg:replacers/sg:replacer[1]/@replacement").isEqualTo("org.new.$1");
        XmlAssert.assertThat(result).withNamespaceContext(ns)
                .valueByXPath("/sg:srcgen4j-config/sg:replacers/sg:replacer[2]/@extension").isEqualTo("java");

    }

    @Test
    public final void testInitReplacers() throws Exception {

        // PREPARE
        final SrcGen4JConfig testee = load("test-replacers.xml");

        // TEST
        testee.init(new DefaultContext(), new File("."));

        // VERIFY variables in the replacer's expression and replacement are resolved
        final List<Replacer> replacers = testee.getReplacers();
        assertThat(replacers).hasSize(2);
        final Replacer rooted = replacers.get(1);
        assertThat(rooted.getExtension()).isEqualTo("java");
        assertThat(rooted.getExpression()).isEqualTo("/var/tmp/(.*)");
        assertThat(rooted.getReplacement()).isEqualTo("/var/tmp/gen/$1");
        assertThat(rooted.replace("/var/tmp/Foo")).isEqualTo("/var/tmp/gen/Foo");

    }

    @Test
    public final void testInit() {

        // PREPARE
        final SrcGen4JConfig testee = new SrcGen4JConfig();

        final Variables vars = new Variables(new Variable("project.name", "1"), new Variable("project.path", "2"),
                new Variable("generator.name", "3"), new Variable("folder.name", "6"), new Variable("folder.path", "7"));

        final List<Project> projects = new ArrayList<Project>();
        final Project project = new Project("${project.name}", "${project.path}");
        projects.add(project);
        project.addFolder(new Folder("${folder.name}", "${folder.path}"));

        final List<GeneratorConfig> genList = new ArrayList<GeneratorConfig>();
        final GeneratorConfig generator = new GeneratorConfig("${generator.name}", "CLASS", "PARSER");
        genList.add(generator);

        final Generators generators = new Generators();
        generators.addVariable(new Variable("a", "1"));
        generators.setList(genList);

        testee.setVariables(vars);
        testee.setProjects(projects);
        testee.setGenerators(generators);

        // TEST
        testee.init(new DefaultContext(), new File("."));

        // VERIFY

        final Project resultProject = testee.getProjects().get(0);
        assertThat(resultProject.getName()).isEqualTo("1");
        assertThat(resultProject.getPath()).isEqualTo("2");
        final Folder resultFolder = resultProject.getFolders().get(0);
        assertThat(resultFolder.getName()).isEqualTo("6");
        assertThat(resultFolder.getPath()).isEqualTo("7");

        assertThat(testee.getGenerators()).isNotNull();
        assertThat(testee.getGenerators().getVarMap()).contains(entry("a", "1"));
        assertThat(testee.getGenerators().getList()).isNotNull();
        assertThat(testee.getGenerators().getList()).hasSize(1);

        final GeneratorConfig resultGenerator = testee.getGenerators().getList().get(0);
        assertThat(resultGenerator.getName()).isEqualTo("3");

    }

    @Test
    public final void testDerivedVariables() throws Exception {

        // PREPARE
        final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class);
        final Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test-variables.xml"));
        try {
            // TEST
            final SrcGen4JConfig testee = JaxbUtils.unmarshal(new UnmarshallerBuilder().withContext(jaxbContext).build(), reader);
            testee.init(new DefaultContext(), new File("."));

            // VERIFY
            assertThat(testee.getVarMap()).containsOnly(entry("rootDir", "."), entry("root", "/var/tmp"), entry("a", "base"),
                    entry("res", "/**\n * Test base.\n */"), entry("escapes", "\r\n\t"));

            final Parsers parsers = testee.getParsers();
            assertThat(parsers.getVarMap()).contains(entry("a", "base/parsers1"), entry("b", "base/parsers1/parsers2"));
            final ParserConfig parserConfig = parsers.getList().get(0);
            // @formatter:off
            assertThat(parserConfig.getVarMap()).contains(
                    entry("a", "base/parsers1/parser1"),
                    entry("b", "base/parsers1/parsers2"),
                    entry("c", "base/parsers1/parser1/parser3"),
                    entry("x", "/**\n * Test base/parsers1/parser1.\n */"),
                    entry("root", "/var/tmp"), entry("rootDir", "."));
            // @formatter:off

            final Generators generators = testee.getGenerators();
            assertThat(generators.getVarMap()).contains(
                    entry("a", "base/generators1"),
                    entry("b", "base/generators1/generators2"));
            final GeneratorConfig generatorConfig = generators.getList().get(0);
            // @formatter:off
            assertThat(generatorConfig.getVarMap()).contains(
                    entry("a", "base/generators1/generator1"),
                    entry("b", "base/generators1/generators2"),
                    entry("c", "base/generators1/generator1/generator3"),
                    entry("root", "/var/tmp"), entry("rootDir", "."));
            // @formatter:off

        } finally {
            reader.close();
        }

    }

    @Test
    public final void testInitNullContent() {

        // PREPARE
        final SrcGen4JConfig testee = new SrcGen4JConfig();

        // TEST
        testee.init(new DefaultContext(), new File("."));

        // VERIFY
        // Test makes sure the "init(File)" does not throw NullPointerException
        // if nothing is set

    }

    @Test
    public void testCreateMavenStyleSingleProject() {

        // PREPARE
        final String projectName = "NAME";

        // TEST
        SrcGen4JConfig config = SrcGen4JConfig.createMavenStyleSingleProject(
                new DefaultContext(), projectName, new File("."));

        // VERIFY
        assertThat(config.getProjects()).hasSize(1);
        final Project project = config.getProjects().get(0);
        assertThat(project.getName()).isEqualTo(projectName);
        assertThat(project.getPath()).isEqualTo(".");
        assertThat(project.isMaven()).isTrue();
        assertThat(project.getFolders()).hasSize(8);
        assertThat(project.getFolders()).contains(new Folder("mainJava", ""),
                new Folder("mainRes", ""), new Folder("genMainJava", ""),
                new Folder("genMainRes", ""), new Folder("testJava", ""),
                new Folder("testRes", ""), new Folder("genTestJava", ""),
                new Folder("genTestRes", ""));

    }

    private SrcGen4JConfig load(final String resourceName) throws Exception {
        final JAXBContext jaxbContext = JAXBContext
                .newInstance(SrcGen4JConfig.class);
        final Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resourceName));
        try {
            return JaxbUtils.unmarshal(new UnmarshallerBuilder().withContext(jaxbContext).build(), reader);
        } finally {
            reader.close();
        }
    }

}
