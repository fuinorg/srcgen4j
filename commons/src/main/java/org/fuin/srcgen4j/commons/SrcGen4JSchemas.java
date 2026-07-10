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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Locates the XSD files that describe a "srcgen4j-config.xml" and combines them into a single schema.
 * <p>
 * Every artifact that contributes a configuration namespace (the commons artifact itself, srcgen4j-core, or any third party
 * parser/generator) declares its XSDs in a {@value #DESCRIPTOR} file. Each line of that file is the classpath resource path of one
 * XSD, without a leading slash. Blank lines and lines starting with '#' are ignored.
 * <p>
 * The commons schema is always placed first because the other schemas import its namespace without a <code>schemaLocation</code>,
 * and {@link SchemaFactory#newSchema(Source[])} resolves such imports only against sources it has already processed.
 */
public final class SrcGen4JSchemas {

    /** Classpath resource that lists the XSDs an artifact contributes. */
    public static final String DESCRIPTOR = "META-INF/srcgen4j/schemas";

    /** Classpath resource of the commons schema. It always comes first, as the other schemas import its namespace. */
    public static final String COMMONS_XSD = "srcgen4j-commons-0_5_0.xsd";

    private SrcGen4JSchemas() {
        throw new UnsupportedOperationException("It is not allowed to instantiate a utility class");
    }

    /**
     * Returns the classpath resource paths of all XSDs found on the given class loader, the commons schema first.
     *
     * @param classLoader
     *            Class loader to search.
     *
     * @return Resource paths without a leading slash, without duplicates.
     */
    public static List<String> findAll(final ClassLoader classLoader) {
        final List<String> paths = new ArrayList<>();
        paths.add(COMMONS_XSD);
        try {
            final Enumeration<URL> descriptors = classLoader.getResources(DESCRIPTOR);
            while (descriptors.hasMoreElements()) {
                for (final String path : readLines(descriptors.nextElement())) {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                }
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error reading the schema descriptors '" + DESCRIPTOR + "'", ex);
        }
        return Collections.unmodifiableList(paths);
    }

    /**
     * Creates the combined schema used to validate a "srcgen4j-config.xml".
     *
     * @param classLoader
     *            Class loader used to locate the XSD resources.
     *
     * @return New schema instance.
     */
    public static Schema createSchema(final ClassLoader classLoader) {
        final List<String> paths = findAll(classLoader);
        final List<Source> sources = new ArrayList<>();
        for (final String path : paths) {
            final URL url = classLoader.getResource(path);
            if (url == null) {
                throw new IllegalStateException(
                        "Schema '" + path + "' is declared in a '" + DESCRIPTOR + "' file, but is not on the classpath");
            }
            final StreamSource source = new StreamSource(url.toExternalForm());
            sources.add(source);
        }
        try {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory.newSchema(sources.toArray(new Source[0]));
        } catch (final SAXException ex) {
            throw new IllegalStateException("Failed to create the schema from: " + paths, ex);
        }
    }

    private static List<String> readLines(final URL url) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (InputStream in = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
    }

}
