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
import java.util.Collections;
import java.util.List;

/**
 * A "srcgen4j-config.xml" does not comply with the srcgen4j XML schema.
 */
public final class InvalidConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    private final transient File configFile;

    private final transient List<ConfigError> errors;

    /**
     * Constructor with configuration file and errors.
     *
     * @param configFile
     *            Configuration file that was validated.
     * @param errors
     *            Errors found in the file - At least one.
     */
    public InvalidConfigException(final File configFile, final List<ConfigError> errors) {
        super(createMessage(configFile, errors));
        this.configFile = configFile;
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns the configuration file that was validated.
     *
     * @return Configuration file.
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Returns the errors found in the configuration file.
     *
     * @return Immutable list with at least one error.
     */
    public List<ConfigError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    private static String createMessage(final File configFile, final List<ConfigError> errors) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid srcgen4j configuration file: ");
        sb.append(configFile);
        sb.append(System.lineSeparator());
        sb.append(errors.size() == 1 ? "1 error:" : errors.size() + " errors:");
        for (final ConfigError error : errors) {
            sb.append(System.lineSeparator());
            sb.append("  ");
            sb.append(error);
        }
        return sb.toString();
    }

    /**
     * A single problem found in the configuration file.
     *
     * @param line
     *            Line the problem was found in or -1 if unknown.
     * @param column
     *            Column the problem was found in or -1 if unknown.
     * @param message
     *            Description of the problem.
     */
    public record ConfigError(int line, int column, String message) {

        @Override
        public String toString() {
            if (line < 0) {
                return message;
            }
            return "[line " + line + ", column " + column + "] " + message;
        }

    }

}
