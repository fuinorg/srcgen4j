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
package org.fuin.srcgen4j.core.velocity;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.core.TrimmedNotEmpty;
import org.fuin.utils4j.Utils4J;

/**
 * File to produce.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "target-file")
public final class TargetFile implements Serializable, Comparable<TargetFile> {

    private static final long serialVersionUID = 1L;

    @TrimmedNotEmpty
    @XmlAttribute
    private String project;

    @TrimmedNotEmpty
    @XmlAttribute
    private String folder;

    @Nullable
    @TrimmedNotEmpty
    @XmlAttribute
    private String path;

    @TrimmedNotEmpty
    @XmlAttribute
    private String name;

    @Nullable
    @Valid
    @XmlElement(name = "argument")
    private List<Argument> arguments;

    /**
     * Default constructor for deserialization.
     */
    @SuppressWarnings("NullAway.Init") // Fields are populated by JAXB after construction
    TargetFile() {
        super();
    }

    /**
     * Constructor with all data.
     *
     * @param project
     *            Name of the target project - Cannot be NULL.
     * @param folder
     *            Name of the target folder inside the project - Cannot be NULL.
     * @param path
     *            Path without filename or NULL.
     * @param name
     *            Name without path - Cannot be NULL.
     * @param args
     *            Arguments for the template or NULL.
     */
    public TargetFile(final String project, final String folder, @Nullable final String path, final String name, final Argument... args) {
        super();
        Contract.requireArgNotNull("project", project);
        Contract.requireArgNotNull("folder", folder);
        Contract.requireArgNotNull("name", name);
        this.project = project;
        this.folder = folder;
        this.path = path;
        this.name = name;

        if (args != null) {
            this.arguments = new ArrayList<Argument>();
            for (final Argument arg : args) {
                this.arguments.add(arg);
            }
        }

    }

    /**
     * Returns the name of the target project.
     *
     * @return Project name.
     */
    public final String getProject() {
        return project;
    }

    /**
     * Returns the name of the target folder inside the project.
     *
     * @return Folder name.
     */
    public final String getFolder() {
        return folder;
    }

    /**
     * Returns the relative path of the target file.
     *
     * @return Path or NULL.
     */
    @Nullable
    public final String getPath() {
        return path;
    }

    /**
     * Returns the name of the target file.
     * 
     * @return Name without path - Never NULL.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns path and name.
     * 
     * @return Relative path and name.
     */
    public final String getPathAndName() {
        if (path == null) {
            return name;
        }
        return path + "/" + name;
    }

    /**
     * Returns a list of arguments to use.
     * 
     * @return Arguments for the template or NULL.
     */
    @Nullable
    public final List<Argument> getArguments() {
        return arguments;
    }

    // CHECKSTYLE:OFF Generated code
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((path == null) ? 0 : path.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TargetFile other = (TargetFile) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    // CHECKSTYLE:ON

    @Override
    public final int compareTo(final TargetFile other) {
        return getPathAndName().compareTo(other.getPathAndName());
    }

    /**
     * Replaces variables (if defined) in the path, name and arguments.
     * 
     * @param vars
     *            Variables to use.
     */
    public final void init(@Nullable final Map<String, String> vars) {
        project = requireNonNull(Utils4J.replaceVars(project, vars));
        folder = requireNonNull(Utils4J.replaceVars(folder, vars));
        path = Utils4J.replaceVars(path, vars);
        name = requireNonNull(Utils4J.replaceVars(name, vars));
        if (arguments != null) {
            for (final Argument argument : arguments) {
                argument.init(vars);
            }
        }
    }

}
