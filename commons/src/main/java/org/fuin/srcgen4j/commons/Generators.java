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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.Contract;

/**
 * Represents a set of code generators.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "generators")
public class Generators extends AbstractElement implements InitializableElement<Generators, SrcGen4JConfig> {

    @Nullable
    @Valid
    @XmlElement(name = "generator")
    private List<GeneratorConfig> list;

    @Nullable
    private transient SrcGen4JConfig parent;

    /**
     * Package visible default constructor for deserialization.
     */
    public Generators() {
        super();
    }

    /**
     * Returns the list of generators.
     * 
     * @return Generators.
     */
    @Nullable
    public final List<GeneratorConfig> getList() {
        return list;
    }

    /**
     * Sets the list of generators.
     * 
     * @param list
     *            Generators.
     */
    public final void setList(@Nullable final List<GeneratorConfig> list) {
        this.list = list;
    }

    /**
     * Adds a generator to the list. If the list does not exist it's created.
     * 
     * @param generator
     *            Generator to add.
     */
    public final void addGenerator(final GeneratorConfig generator) {
        Contract.requireArgNotNull("generator", generator);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(generator);
    }

    /**
     * Returns the parent of the object.
     * 
     * @return GeneratorConfig.
     */
    @Nullable
    public final SrcGen4JConfig getParent() {
        return parent;
    }

    @Override
    public final Generators init(final SrcGen4JContext context, final SrcGen4JConfig parent, @Nullable final Map<String, String> vars) {
        this.parent = parent;
        inheritVariables(vars);
        if (list != null) {
            for (final GeneratorConfig generator : list) {
                generator.init(context, this, getVarMap());
            }
        }
        return this;
    }

    /**
     * Returns a generator by it's name.
     * 
     * @param generatorName
     *            Name to find.
     * 
     * @return The generator.
     * 
     * @throws GeneratorNotFoundException
     *             No generator with the given name was found.
     */
    @Nullable
    public final GeneratorConfig findByName(@NotEmpty final String generatorName) throws GeneratorNotFoundException {
        Contract.requireArgNotEmpty("generatorName", generatorName);
        if (list == null) {
            throw new GeneratorNotFoundException(generatorName);
        }
        final int idx = list.indexOf(new GeneratorConfig(generatorName, "dummy", "dummy"));
        if (idx < 0) {
            throw new GeneratorNotFoundException(generatorName);
        }
        return list.get(idx);
    }

}
