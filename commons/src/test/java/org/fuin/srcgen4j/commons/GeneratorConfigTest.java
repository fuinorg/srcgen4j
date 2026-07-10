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

import java.util.HashMap;
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
 * Tests for {@link GeneratorConfig}.
 */
public class GeneratorConfigTest extends AbstractTest {

    @Test
    public final void testPojoStructureAndBehavior() {

        final PojoClass pc = PojoClassFactory.getPojoClass(GeneratorConfig.class);
        final Validator validator = createPojoValidatorBuilder().build();
        validator.validate(pc);

    }

    @Test
    public final void testMarshal() throws Exception {

        // PREPARE
        final JAXBContext jaxbContext = JAXBContext.newInstance(GeneratorConfig.class);
        final GeneratorConfig testee = new GeneratorConfig("NAME", "CLASS", "PARSER");

        // TEST
        final String result = new JaxbHelper(false).write(testee, jaxbContext);

        // VERIFY
        XmlAssert.assertThat(result)
                .and(XML + "<sg4jc:generator class=\"CLASS\" parser=\"PARSER\" name=\"NAME\"" + " xmlns:sg4jc=\"" + NS_SG4JC + "\"/>")
                .areIdentical();

    }

    @Test
    public final void testUnmarshal() throws Exception {

        // PREPARE
        final JAXBContext jaxbContext = JAXBContext.newInstance(GeneratorConfig.class);

        // TEST
        final GeneratorConfig testee = JaxbUtils.unmarshal(new UnmarshallerBuilder().withContext(jaxbContext).build(),
                "<generator name=\"abc\" class=\"CLASS\" parser=\"PARSER\" xmlns=\"" + NS_SG4JC + "\"/>");

        // VERIFY
        assertThat(testee).isNotNull();
        assertThat(testee.getName()).isEqualTo("abc");
        assertThat(testee.getClassName()).isEqualTo("CLASS");
        assertThat(testee.getParser()).isEqualTo("PARSER");

    }

    @Test
    public final void testInit() {

        // PREPARE
        final Generators parent = new Generators();
        final GeneratorConfig testee = new GeneratorConfig("A${a}A", "CLASS", "PARSER");

        final Map<String, String> vars = new HashMap<String, String>();
        vars.put("a", "1");

        // TEST
        testee.init(new DefaultContext(), parent, vars);

        // VERIFY
        assertThat(testee.getParent()).isSameAs(parent);
        assertThat(testee.getName()).isEqualTo("A1A");

    }

}
