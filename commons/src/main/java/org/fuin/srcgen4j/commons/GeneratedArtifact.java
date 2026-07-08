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

import jakarta.validation.constraints.NotEmpty;

import org.fuin.objects4j.common.Contract;

/**
 * Generated artifact.
 */
public final class GeneratedArtifact {

    private final String name;

    private final String pathAndName;

    private final byte[] data;

    private final String project;

    private final String folder;

    /**
     * Constructor with all data.
     *
     * @param name
     *            Unique artifact name.
     * @param pathAndName
     *            Relative path and filename to write the source code to.
     * @param data
     *            Generated data.
     * @param project
     *            Name of the target project.
     * @param folder
     *            Name of the target folder inside the project.
     */
    public GeneratedArtifact(@NotEmpty final String name, @NotEmpty final String pathAndName, final byte[] data,
            @NotEmpty final String project, @NotEmpty final String folder) {
        super();
        Contract.requireArgNotEmpty("name", name);
        Contract.requireArgNotEmpty("pathAndName", pathAndName);
        Contract.requireArgNotNull("data", data);
        Contract.requireArgNotEmpty("project", project);
        Contract.requireArgNotEmpty("folder", folder);
        this.name = name;
        this.pathAndName = pathAndName;
        this.data = data;
        this.project = project;
        this.folder = folder;
    }

    /**
     * Returns the unique artifact name.
     * 
     * @return Name.
     */
    @NotEmpty
    public final String getName() {
        return name;
    }

    /**
     * Returns the relative path and filename.
     * 
     * @return Path and filename to write the source code to.
     */
    @NotEmpty
    public final String getPathAndName() {
        return pathAndName;
    }

    /**
     * Returns the generated data (source code).
     * 
     * @return Data.
     */
    public final byte[] getData() {
        return data;
    }

    /**
     * Returns the name of the target project.
     *
     * @return Project name.
     */
    @NotEmpty
    public final String getProject() {
        return project;
    }

    /**
     * Returns the name of the target folder inside the project.
     *
     * @return Folder name.
     */
    @NotEmpty
    public final String getFolder() {
        return folder;
    }

    @Override
    public final String toString() {
        return project + "/" + folder + "/" + pathAndName + " [" + name + "]";
    }

}
