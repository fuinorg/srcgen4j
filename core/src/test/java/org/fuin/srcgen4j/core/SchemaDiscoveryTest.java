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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.fuin.srcgen4j.commons.InvalidConfigException;
import org.fuin.srcgen4j.commons.SrcGen4JConfigValidator;
import org.fuin.srcgen4j.commons.SrcGen4JSchemas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that this artifact contributes its XSDs to {@link SrcGen4JSchemas}.
 * <p>
 * Without the "META-INF/srcgen4j/schemas" descriptor the wildcard for a parser or generator <code>&lt;config&gt;</code> is "lax",
 * so an invalid extension config would silently pass instead of failing. That would be easy to miss, hence these tests.
 */
public class SchemaDiscoveryTest {

    private static ClassLoader classLoader() {
        return SchemaDiscoveryTest.class.getClassLoader();
    }

    @Test
    public final void testCoreSchemasAreDiscovered() {

        // TEST
        final List<String> paths = SrcGen4JSchemas.findAll(classLoader());

        // VERIFY
        assertThat(paths.get(0)).isEqualTo(SrcGen4JSchemas.COMMONS_XSD);
        assertThat(paths).contains("srcgen4j-core-base-0_5_0.xsd", "srcgen4j-core-emf-0_5_0.xsd", "srcgen4j-core-xtext-0_5_0.xsd",
                "srcgen4j-core-velocity-0_5_0.xsd");

    }

    @Test
    public final void testValidConfigWithExtensionConfig() throws InvalidConfigException {

        SrcGen4JConfigValidator.validate(new File("src/test/resources/domain/xtext-test-config.xml"), classLoader());

    }

    @Test
    public final void testInvalidExtensionConfigIsRejected(@TempDir final Path tempDir) throws IOException {

        // PREPARE - the error is inside the "emf" namespace, so it is only found if the emf schema was discovered
        final File configFile = tempDir.resolve("srcgen4j-config.xml").toFile();
        Files.writeString(configFile.toPath(), """
                <?xml version="1.0" encoding="UTF-8"?>
                <srcgen4j-config xmlns="http://www.fuin.org/srcgen4j/commons/0.5.0"
                                 xmlns:emf="http://www.fuin.org/srcgen4j/core/emf/0.5.0">
                	<modules>
                		<module name="current" path="." maven="false" />
                	</modules>
                	<parsers>
                		<parser name="p1" class="a.b.C" />
                	</parsers>
                	<generators>
                		<generator name="g1" class="a.b.D" parser="p1">
                			<config>
                				<emf:emf-generator-config>
                					<emf:artifact-factory bogus="x" artifact="a1" class="a.b.E" />
                				</emf:emf-generator-config>
                			</config>
                		</generator>
                	</generators>
                </srcgen4j-config>
                """);

        // TEST
        final InvalidConfigException ex = catchThrowableOfType(InvalidConfigException.class,
                () -> SrcGen4JConfigValidator.validate(configFile, classLoader()));

        // VERIFY
        assertThat(ex).isNotNull();
        assertThat(ex.getErrors()).hasSize(1);
        assertThat(ex.getErrors().get(0).message()).contains("bogus").contains("artifact-factory");

    }

}
