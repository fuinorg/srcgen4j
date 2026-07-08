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
package org.fuin.srcgen4j.core;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Builds the combined srcgen4j XSD schema (commons + core base/emf/xtext) for tests.
 */
public final class SchemaHelper {

    private SchemaHelper() {
        throw new UnsupportedOperationException("It is not allowed to instantiate a utility class");
    }

    /**
     * Creates the combined schema for validating a full srcgen4j configuration.
     * <p>
     * The order of the sources is significant: {@code SchemaFactory.newSchema(Source[])} processes them sequentially,
     * and because the emf schema imports the commons namespace, the <b>commons schema must come first</b> so its
     * declarations are available when the emf schema's <code>&lt;xs:import&gt;</code> is resolved (by namespace, without
     * any network access). This is why the schema cannot be built via {@code UnmarshallerBuilder.addClasspathSchemas},
     * which keeps its sources in an unordered set.
     *
     * @return New schema instance.
     */
    public static Schema createCoreSchema() {
        try {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory.newSchema(new Source[] {
                    new StreamSource(SchemaHelper.class.getResourceAsStream("/srcgen4j-commons-0_5_0.xsd")),
                    new StreamSource(SchemaHelper.class.getResourceAsStream("/srcgen4j-core-base-0_5_0.xsd")),
                    new StreamSource(SchemaHelper.class.getResourceAsStream("/srcgen4j-core-emf-0_5_0.xsd")),
                    new StreamSource(SchemaHelper.class.getResourceAsStream("/srcgen4j-core-xtext-0_5_0.xsd")) });
        } catch (final SAXException ex) {
            throw new RuntimeException("Failed to create schema", ex);
        }
    }

}
