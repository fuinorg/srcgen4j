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

import static java.util.Objects.requireNonNull;
import static org.fuin.utils4j.Utils4J.replaceVars;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.Contract;
import org.fuin.utils4j.Utils4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a code generator.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "generator")
public final class GeneratorConfig extends AbstractNamedElement implements InitializableElement<GeneratorConfig, Generators> {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorConfig.class);

    @NotNull
    @XmlAttribute(name = "class")
    private String className;

    @NotNull
    @XmlAttribute(name = "parser")
    private String parser;

    @Nullable
    @Valid
    @XmlElement(name = "config")
    private Config<GeneratorConfig> config;

    @Nullable
    @XmlTransient
    private SrcGen4JContext context;

    @Nullable
    @XmlTransient
    private Generators parent;

    @Nullable
    @XmlTransient
    private Generator<Object> generator;

    /**
     * Package visible default constructor for deserialization.
     */
    @SuppressWarnings("NullAway.Init") // Fields are populated by JAXB after construction
    GeneratorConfig() { // NOSONAR Ignore not initialized fields
        super();
    }

    /**
     * Constructor with name.
     *
     * @param name
     *            Name to set.
     * @param className
     *            Full qualified name of the generator class to set.
     * @param parser
     *            Unique name of the parser that delivers the input for this generator to set.
     */
    public GeneratorConfig(@NotEmpty final String name, @NotEmpty final String className, @NotEmpty final String parser) {
        super(name);
        Contract.requireArgNotNull("className", className);
        Contract.requireArgNotNull("parser", parser);
        this.className = className;
        this.parser = parser;
    }

    /**
     * Returns the name of the generator class.
     * 
     * @return Full qualified class name.
     */
    @NotEmpty
    public final String getClassName() {
        return className;
    }

    /**
     * Returns the name of the parser that delivers the input for this generator.
     * 
     * @return Unique parser name.
     */
    @NotEmpty
    public final String getParser() {
        return parser;
    }

    /**
     * Returns the generator specific configuration.
     * 
     * @return Configuration for the parser.
     */
    @Nullable
    public final Config<GeneratorConfig> getConfig() {
        return config;
    }

    /**
     * Sets the generator specific configuration.
     * 
     * @param config
     *            Configuration for the parser.
     */
    public final void setConfig(@Nullable final Config<GeneratorConfig> config) {
        this.config = config;
    }

    /**
     * Returns the parent of the object.
     * 
     * @return Generators.
     */
    @Nullable
    public final Generators getParent() {
        return parent;
    }

    /**
     * Sets the parent of the object.
     * 
     * @param parent
     *            Generators.
     */
    public final void setParent(@Nullable final Generators parent) {
        this.parent = parent;
    }

    @Override
    public final GeneratorConfig init(final SrcGen4JContext context, final Generators parent, @Nullable final Map<String, String> vars) {
        this.context = context;
        this.parent = parent;
        inheritVariables(vars);
        setName(requireNonNull(replaceVars(getName(), getVarMap())));
        if (config != null) {
            config.init(context, this, getVarMap());
        }
        return this;
    }

    /**
     * Returns an existing generator instance or creates a new one if it's the first call to this method.
     * 
     * @return Generator of type {@link #className}.
     */
    @SuppressWarnings("unchecked")
    public final Generator<Object> getGenerator() {
        if (generator != null) {
            return generator;
        }
        LOG.info("Creating generator: {}", className);
        if (className == null) {
            throw new IllegalStateException("Class name was not set: " + getName());
        }
        if (context == null) {
            throw new IllegalStateException("Context class loader was not set: " + getName() + " / " + className);
        }
        final Object obj = Utils4J.createInstance(className, context.getClassLoader());
        if (!(obj instanceof Generator<?>)) {
            throw new IllegalStateException("Expected class to be of type '" + Generator.class.getName() + "', but was: " + className);
        }
        generator = (Generator<Object>) obj;
        generator.initialize(this);
        return generator;
    }

    /**
     * Resolves the folder for a given module and folder name using the root configuration.
     *
     * @param moduleName
     *            Name of the target module.
     * @param folderName
     *            Name of the target folder inside the module.
     *
     * @return Target folder or NULL if it could not be found.
     */
    @Nullable
    public final Folder findFolder(final String moduleName, final String folderName) {
        if (parent == null) {
            throw new IllegalStateException("Parent (generators) for generator config is not set: " + getName());
        }
        final SrcGen4JConfig config = parent.getParent();
        if (config == null) {
            throw new IllegalStateException("Root configuration for generator config is not set: " + getName());
        }
        return config.findFolder(moduleName, folderName);
    }

    /**
     * Returns the context the configuration belongs to.
     * 
     * @return Current context.
     */
    @Nullable
    public final SrcGen4JContext getContext() {
        return context;
    }

}
