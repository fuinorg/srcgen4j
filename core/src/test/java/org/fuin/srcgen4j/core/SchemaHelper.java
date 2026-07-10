package org.fuin.srcgen4j.core;

import javax.xml.validation.Schema;

import org.fuin.srcgen4j.commons.SrcGen4JSchemas;

/**
 * Builds the combined srcgen4j XSD schema for tests.
 */
public final class SchemaHelper {

    private SchemaHelper() {
        throw new UnsupportedOperationException("It is not allowed to instantiate a utility class");
    }

    /**
     * Creates the combined schema for validating a full srcgen4j configuration. It contains every XSD that the artifacts on the
     * classpath contribute, in the order required to resolve their imports.
     *
     * @return New schema instance.
     */
    public static Schema createCoreSchema() {
        return SrcGen4JSchemas.createSchema(SchemaHelper.class.getClassLoader());
    }

}
