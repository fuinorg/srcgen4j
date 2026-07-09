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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.fuin.objects4j.common.Contract;
import org.fuin.srcgen4j.commons.InvalidConfigException.ConfigError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates a "srcgen4j-config.xml" against the srcgen4j XML schema before it is used.
 * <p>
 * The schema is assembled from all XSDs found on the classpath (see {@link SrcGen4JSchemas}). The configuration of a parser or
 * generator (the <code>&lt;config&gt;</code> element) is validated if a schema for its namespace is available, and skipped
 * otherwise, so that third party extensions without an XSD do not cause a failure.
 */
public final class SrcGen4JConfigValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SrcGen4JConfigValidator.class);

    private SrcGen4JConfigValidator() {
        throw new UnsupportedOperationException("It is not allowed to instantiate a utility class");
    }

    /**
     * Validates the given configuration file and reports all problems that were found.
     *
     * @param configFile
     *            Configuration file to validate - Cannot be NULL and has to exist.
     * @param classLoader
     *            Class loader used to locate the XSD resources - Cannot be NULL.
     *
     * @throws InvalidConfigException
     *             The file does not comply with the schema. The message names the file and lists every problem with its line and
     *             column.
     */
    public static void validate(final File configFile, final ClassLoader classLoader) throws InvalidConfigException {

        Contract.requireArgNotNull("configFile", configFile);
        Contract.requireArgNotNull("classLoader", classLoader);

        final List<ConfigError> errors = new ArrayList<>();
        final Validator validator = SrcGen4JSchemas.createSchema(classLoader).newValidator();
        validator.setErrorHandler(new CollectingErrorHandler(errors));

        try {
            validator.validate(new StreamSource(configFile));
        } catch (final SAXException ex) {
            // Thrown for a fatal error (like a syntax error) after the handler has seen it. A malformed
            // document stops the parser, so anything already collected is all there is to report.
            if (errors.isEmpty()) {
                errors.add(new ConfigError(-1, -1, messageOf(ex)));
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException("Error reading the configuration file: " + configFile, ex);
        }

        if (!errors.isEmpty()) {
            throw new InvalidConfigException(configFile, errors);
        }

    }

    private static String messageOf(final Throwable ex) {
        final String message = ex.getMessage();
        return message == null ? ex.toString() : message;
    }

    /**
     * Collects all errors instead of aborting on the first one, so the user sees every problem in one run.
     */
    private static final class CollectingErrorHandler implements ErrorHandler {

        private final List<ConfigError> errors;

        private CollectingErrorHandler(final List<ConfigError> errors) {
            this.errors = errors;
        }

        @Override
        public void warning(final SAXParseException ex) {
            LOG.debug("Warning validating the configuration file", ex);
        }

        @Override
        public void error(final SAXParseException ex) {
            errors.add(toError(ex));
        }

        @Override
        public void fatalError(final SAXParseException ex) throws SAXException {
            errors.add(toError(ex));
            throw ex;
        }

        private static ConfigError toError(final SAXParseException ex) {
            return new ConfigError(ex.getLineNumber(), ex.getColumnNumber(), messageOf(ex));
        }

    }

}
