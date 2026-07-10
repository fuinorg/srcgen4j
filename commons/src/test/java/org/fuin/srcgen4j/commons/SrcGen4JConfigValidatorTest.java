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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fuin.srcgen4j.commons.InvalidConfigException.ConfigError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link SrcGen4JConfigValidator}.
 */
public class SrcGen4JConfigValidatorTest {

    private static final File TEST_RESOURCES = new File("src/test/resources");

    private static ClassLoader classLoader() {
        return SrcGen4JConfigValidatorTest.class.getClassLoader();
    }

    @Test
    public final void testValidConfig() throws InvalidConfigException {

        // The file uses a "test:input" parser config from a namespace that has no schema on the classpath.
        // The wildcard is "lax", so that unknown extension config is skipped instead of failing.
        SrcGen4JConfigValidator.validate(new File(TEST_RESOURCES, "test-config.xml"), classLoader());

    }

    @Test
    public final void testInvalidConfigReportsAllErrors() {

        // PREPARE
        final File configFile = new File(TEST_RESOURCES, "invalid-config.xml");

        // TEST
        final InvalidConfigException ex = catchThrowableOfType(InvalidConfigException.class,
                () -> SrcGen4JConfigValidator.validate(configFile, classLoader()));

        // VERIFY - both problems are reported in a single run, not just the first one
        assertThat(ex).isNotNull();
        assertThat(ex.getConfigFile()).isEqualTo(configFile);
        assertThat(ex.getErrors()).hasSize(2);

        final ConfigError first = ex.getErrors().get(0);
        assertThat(first.line()).isEqualTo(6);
        assertThat(first.message()).contains("modules").contains("module");

        final ConfigError second = ex.getErrors().get(1);
        assertThat(second.line()).isEqualTo(15);
        assertThat(second.message()).contains("module").contains("generator");

        // The message has to name the file and every problem with its position
        assertThat(ex.getMessage()).contains("invalid-config.xml").contains("2 errors:").contains("[line 6, column").contains(
                "[line 15, column");

    }

    @Test
    public final void testMalformedXml(@TempDir final Path tempDir) throws IOException {

        // PREPARE
        final File configFile = tempDir.resolve("srcgen4j-config.xml").toFile();
        Files.writeString(configFile.toPath(), "<srcgen4j-config xmlns=\"" + SrcGen4JCommonsNamespace.NAMESPACE + "\">");

        // TEST & VERIFY - a syntax error is reported as a normal config error, not as a stack trace
        assertThatThrownBy(() -> SrcGen4JConfigValidator.validate(configFile, classLoader()))
                .isInstanceOf(InvalidConfigException.class).hasMessageContaining("srcgen4j-config.xml");

    }

    @Test
    public final void testUnknownRootElement(@TempDir final Path tempDir) throws IOException {

        // PREPARE
        final File configFile = tempDir.resolve("srcgen4j-config.xml").toFile();
        Files.writeString(configFile.toPath(), "<wrong-root xmlns=\"" + SrcGen4JCommonsNamespace.NAMESPACE + "\"/>");

        // TEST
        final InvalidConfigException ex = catchThrowableOfType(InvalidConfigException.class,
                () -> SrcGen4JConfigValidator.validate(configFile, classLoader()));

        // VERIFY
        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("wrong-root");

    }

}
