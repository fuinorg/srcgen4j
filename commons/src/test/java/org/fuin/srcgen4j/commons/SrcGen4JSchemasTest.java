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

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SrcGen4JSchemas}.
 */
public class SrcGen4JSchemasTest {

    @Test
    public final void testFindAllReturnsCommonsSchemaFirst() {

        // TEST
        final List<String> paths = SrcGen4JSchemas.findAll(getClass().getClassLoader());

        // VERIFY - the other schemas import the commons namespace without a "schemaLocation",
        // so the commons schema has to be processed before them
        assertThat(paths).isNotEmpty();
        assertThat(paths.get(0)).isEqualTo(SrcGen4JSchemas.COMMONS_XSD);
        assertThat(paths).doesNotHaveDuplicates();

    }

    @Test
    public final void testCreateSchema() {

        assertThat(SrcGen4JSchemas.createSchema(getClass().getClassLoader())).isNotNull();

    }

}
