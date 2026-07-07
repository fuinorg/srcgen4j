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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fuin.srcgen4j.commons.TestUtils.NS_SG4JC;
import static org.fuin.srcgen4j.commons.TestUtils.createPojoValidator;
import static org.fuin.utils4j.jaxb.JaxbUtils.XML_PREFIX;
import static org.fuin.utils4j.jaxb.JaxbUtils.marshal;
import static org.fuin.utils4j.jaxb.JaxbUtils.unmarshal;

import java.util.Map;
import java.util.regex.Pattern;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.fuin.utils4j.jaxb.UnmarshallerBuilder;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;

/**
 * Tests for {@link Replacer}.
 */
public class ReplacerTest {

    @Test
    public final void testPojoStructureAndBehavior() {

        final PojoClass pc = PojoClassFactory.getPojoClass(Replacer.class);
        final Validator pv = createPojoValidator();
        pv.validate(pc);

    }

    @Test
    public final void testMarshal() throws Exception {

        // PREPARE
        final Replacer testee = new Replacer("nm", "ext", "a(b)", "x$1");

        // TEST
        final String result = marshal(testee, Replacer.class);

        // VERIFY
        final String expected = """
                <sg4jc:replacer
                    name="nm"
                    extension="ext"
                    expression="a(b)"
                    replacement="x$1"
                    xmlns:sg4jc="%s"/>""".formatted(NS_SG4JC);
        XmlAssert.assertThat(result).and(XML_PREFIX + expected).areIdentical();

    }

    @Test
    public final void testUnmarshal() throws Exception {

        // TEST
        final String xml = """
                <sg4jc:replacer
                    name="nm"
                    extension="ext"
                    expression="a(b)"
                    replacement="x$1"
                    xmlns:sg4jc="%s"/>""".formatted(NS_SG4JC);
        final Replacer testee = unmarshal(new UnmarshallerBuilder().addClassesToBeBound(Replacer.class).build(), xml);

        // VERIFY
        assertThat(testee).isNotNull();
        assertThat(testee.getName()).isEqualTo("nm");
        assertThat(testee.getExtension()).isEqualTo("ext");
        assertThat(testee.getExpression()).isEqualTo("a(b)");
        assertThat(testee.getReplacement()).isEqualTo("x$1");

    }

    @Test
    public final void testHashCodeEquals() {
        EqualsVerifier.forClass(Replacer.class)
                .withIgnoredFields("extension", "expression", "replacement", "expressionPattern")
                .withPrefabValues(Pattern.class, Pattern.compile("aaa"), Pattern.compile("bbb"))
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public final void testReplace() {

        // PREPARE
        final Replacer testee = new Replacer("package",
                "org\\.fuin\\.examples\\.cqrskeycloak\\.(.*)",
                "org.fuin.examples.cqrskeycloak.shared.domain.$1");

        // TEST & VERIFY
        assertThat(testee.replace("org.fuin.examples.cqrskeycloak.Foo"))
                .isEqualTo("org.fuin.examples.cqrskeycloak.shared.domain.Foo");

    }

    @Test
    public final void testReplaceMultipleGroups() {

        // PREPARE
        final Replacer testee = new Replacer("swap", "(a)(b)", "$2$1");

        // TEST & VERIFY
        assertThat(testee.replace("ab")).isEqualTo("ba");

    }

    @Test
    public final void testReplaceNamedGroup() {

        // PREPARE
        final Replacer testee = new Replacer("named", "(?<first>a)(?<second>b)", "${second}${first}");

        // TEST & VERIFY
        assertThat(testee.replace("ab")).isEqualTo("ba");

    }

    @Test
    public final void testInit() {

        // PREPARE
        final Replacer testee = new Replacer("pkg", "${ext}", "org\\.old\\.(.*)", "org.new.$1");
        final Map<String, String> vars = Map.of("ext", "java");

        // TEST
        final Replacer result = testee.init(vars);

        // VERIFY
        assertThat(result).isSameAs(testee);
        assertThat(testee.getName()).isEqualTo("pkg");
        assertThat(testee.getExtension()).isEqualTo("java");
        assertThat(testee.getExpression()).isEqualTo("org\\.old\\.(.*)");
        assertThat(testee.getReplacement()).isEqualTo("org.new.$1");
        // The (recompiled) expression is still usable after init
        assertThat(testee.replace("org.old.Foo")).isEqualTo("org.new.Foo");

    }

    @Test
    public final void testConstructorDefersValidation() {

        // Like Target, the constructor only stores the values. An (as yet unresolved) invalid
        // expression or replacement therefore does not fail on construction, only on init/use.
        assertThatNoException().isThrownBy(() -> new Replacer("bad", "a[", "$9"));
        assertThatNoException().isThrownBy(() -> new Replacer("vars", "${root}\\.(.*)", "${root}.$1"));

    }

    @Test
    public final void testInitInvalidExpression() {
        assertThatThrownBy(() -> new Replacer("bad", "a[", "b").init(Map.of())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'expression' is not a valid regular expression");
    }

    @Test
    public final void testInitReplacementDanglingEscape() {
        assertThatThrownBy(() -> new Replacer("bad", "a", "b\\").init(Map.of())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'replacement' is not a valid replacement string");
    }

    @Test
    public final void testInitReplacementIllegalGroupReference() {
        assertThatThrownBy(() -> new Replacer("bad", "a", "$x").init(Map.of())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'replacement' is not a valid replacement string");
    }

    @Test
    public final void testInitReplacementUnknownNumberedGroup() {
        assertThatThrownBy(() -> new Replacer("bad", "abc", "$1").init(Map.of())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("there is no group 1 in the expression");
    }

    @Test
    public final void testInitReplacementUnknownNamedGroup() {
        assertThatThrownBy(() -> new Replacer("bad", "(a)", "${missing}").init(Map.of())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("there is no group with name 'missing' in the expression");
    }

}
