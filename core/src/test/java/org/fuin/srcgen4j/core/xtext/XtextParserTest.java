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
package org.fuin.srcgen4j.core.xtext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.fuin.srcgen4j.commons.DefaultContext;
import org.fuin.srcgen4j.commons.ParseException;
import org.fuin.srcgen4j.commons.ParserConfig;
import org.fuin.srcgen4j.commons.SrcGen4JConfig;
import org.fuin.srcgen4j.core.SchemaHelper;
import org.fuin.srcgen4j.core.emf.EMFGeneratorConfig;
import org.fuin.utils4j.classpath.Handler;
import org.fuin.utils4j.jaxb.JaxbUtils;
import org.fuin.utils4j.jaxb.UnmarshallerBuilder;
import org.fuin.xsample.xSampleDsl.Greeting;
import org.fuin.xsample.xSampleDsl.XSampleDslPackage;
import org.fuin.xsample.xSampleDsl.impl.GreetingImpl;
import org.fuin.xsample.xSampleDsl.impl.ModelImpl;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link XtextParser}.
 */
public class XtextParserTest {

    // CHECKSTYLE:OFF

    private XtextParser createTestee() throws Exception {

        Handler.add();

        final DefaultContext context = new DefaultContext();
        final File dir = new File("src/test/resources/domain");
        final File file = new File(dir, "xtext-test-config.xml");
        final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class, XtextParserConfig.class, EMFGeneratorConfig.class);
        final Unmarshaller unmarshaller = new UnmarshallerBuilder().withContext(jaxbContext).build();
        // Validate against the XSDs (see SchemaHelper - the commons schema must be first because the emf schema imports it).
        unmarshaller.setSchema(SchemaHelper.createCoreSchema());
        final SrcGen4JConfig srcGen4JConfig = JaxbUtils.unmarshal(unmarshaller, file);
        srcGen4JConfig.init(context, new File("."));
        final ParserConfig config = srcGen4JConfig.getParsers().getList().get(0);

        final XtextParser testee = new XtextParser();
        testee.initialize(context, config);
        return testee;
    }

    @Test
    public void testParse() throws Exception {

        final XtextParser testee = createTestee();

        // TEST
        final ResourceSet resourceSet = testee.parse();

        // VERIFY
        assertThat(resourceSet).isNotNull();

        final TreeIterator<Notifier> it = resourceSet.getAllContents();
        assertThat(it.next()).isInstanceOf(LazyLinkingResource.class);
        assertThat(it.next()).isInstanceOf(ModelImpl.class);
        final Notifier notifier = it.next();
        assertThat(notifier).isInstanceOf(GreetingImpl.class);
        final Greeting greeting = (Greeting) notifier;
        assertThat(greeting.getName()).isEqualTo("World");

    }

    /**
     * A model that parses and links fine but violates a validation rule must fail the build. The
     * sample DSL has no rules of its own, so one is registered for the duration of the test.
     */
    @Test
    public void testParseFailsOnValidationError() throws Exception {

        final XtextParser testee = createTestee();

        final EPackage ePackage = XSampleDslPackage.eINSTANCE;
        final Object previous = EValidator.Registry.INSTANCE.get(ePackage);
        EValidator.Registry.INSTANCE.put(ePackage, new RejectWorldValidator());
        try {

            // TEST & VERIFY
            assertThatThrownBy(testee::parse)
                    .isInstanceOf(ParseException.class)
                    // The message must name the problem and where it is - the log is not enough
                    .hasMessageContaining("Greetings to 'World' are not allowed")
                    .hasMessageContaining("xtext-test.xsdsl")
                    .hasMessageContaining("line 1");

        } finally {
            EValidator.Registry.INSTANCE.remove(ePackage);
            if (previous != null) {
                EValidator.Registry.INSTANCE.put(ePackage, previous);
            }
        }

    }

    /** Reports an error for every greeting to "World". */
    private static final class RejectWorldValidator implements EValidator {

        @Override
        public boolean validate(final EObject eObject, final DiagnosticChain diagnostics, final Map<Object, Object> context) {
            if ((eObject instanceof Greeting) && "World".equals(((Greeting) eObject).getName())) {
                // The EObject is passed as data so that Xtext can determine the location
                diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, "test", 0, "Greetings to 'World' are not allowed",
                        new Object[] { eObject }));
                return false;
            }
            return true;
        }

        @Override
        public boolean validate(final EClass eClass, final EObject eObject, final DiagnosticChain diagnostics,
                final Map<Object, Object> context) {
            return validate(eObject, diagnostics, context);
        }

        @Override
        public boolean validate(final EDataType eDataType, final Object value, final DiagnosticChain diagnostics,
                final Map<Object, Object> context) {
            return true;
        }

    }

    // CHECKSTYLE:ON

}
