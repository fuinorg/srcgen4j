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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.FileExists;
import org.fuin.objects4j.common.FileExistsValidator;
import org.fuin.objects4j.common.IsDirectory;
import org.fuin.objects4j.common.IsDirectoryValidator;
import org.fuin.utils4j.VariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration that maps generator output to projects.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "srcgen4j-config")
public class SrcGen4JConfig {

    private static final String ROOT_DIR_VAR = "rootDir";

    private static final Logger LOG = LoggerFactory.getLogger(SrcGen4JConfig.class);

    @Nullable
    @Valid
    @XmlElement(name = "variables")
    private Variables variables;

    @Nullable
    @Valid
    @XmlElementWrapper(name = "projects")
    @XmlElement(name = "project")
    private List<Project> projects;

    @Nullable
    @Valid
    @XmlElementWrapper(name = "replacers")
    @XmlElement(name = "replacer")
    private List<Replacer> replacers;

    @Nullable
    @Valid
    @XmlElement(name = "parsers")
    private Parsers parsers;

    @Nullable
    @Valid
    @XmlElement(name = "generators")
    private Generators generators;

    @Nullable
    @XmlTransient
    private Map<String, String> varMap;

    @XmlTransient
    private boolean initialized = false;

    /**
     * Default constructor.
     */
    public SrcGen4JConfig() {
        super();
    }

    /**
     * Returns a list of variables.
     * 
     * @return Variables.
     */
    @Nullable
    public final Variables getVariables() {
        return variables;
    }

    /**
     * Returns a map of variables.
     * 
     * @return Map of variables.
     */
    @Nullable
    public final Map<String, String> getVarMap() {
        return varMap;
    }

    /**
     * Sets a list of variables.
     * 
     * @param variables
     *            Variables.
     */
    public final void setVariables(@Nullable final Variables variables) {
        this.variables = variables;
    }

    /**
     * Returns a list of projects.
     * 
     * @return Projects.
     */
    @Nullable
    public final List<Project> getProjects() {
        return projects;
    }

    /**
     * Sets a list of projects.
     * 
     * @param projects
     *            Projects.
     */
    public final void setProjects(@Nullable final List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Returns a list of replacers.
     *
     * @return Replacers.
     */
    @Nullable
    public final List<Replacer> getReplacers() {
        return replacers;
    }

    /**
     * Sets a list of replacers.
     *
     * @param replacers
     *            Replacers.
     */
    public final void setReplacers(@Nullable final List<Replacer> replacers) {
        this.replacers = replacers;
    }

    /**
     * Returns the list of parsers.
     * 
     * @return Parsers.
     */
    @Nullable
    public final Parsers getParsers() {
        return parsers;
    }

    /**
     * Sets the list of parsers.
     * 
     * @param parsers
     *            Parsers.
     */
    public final void setParsers(@Nullable final Parsers parsers) {
        this.parsers = parsers;
    }

    /**
     * Adds a parser to the configuration. If the list of parsers does not exist it will be created.
     * 
     * @param parser
     *            Parser to add.
     */
    public final void addParser(final ParserConfig parser) {
        Contract.requireArgNotNull("parser", parser);
        if (parsers == null) {
            parsers = new Parsers();
        }
        parsers.addParser(parser);
    }

    /**
     * Returns a set of generators.
     * 
     * @return Generators.
     */
    @Nullable
    public final Generators getGenerators() {
        return generators;
    }

    /**
     * Sets a the of generators.
     * 
     * @param generators
     *            Generators.
     */
    public final void setGenerators(@Nullable final Generators generators) {
        this.generators = generators;
    }

    /**
     * Returns the information if the object has been initialized.
     * 
     * @return If the method {@link #init(SrcGen4JContext, File)} was called TRUE, else FALSE.
     */
    public final boolean isInitialized() {
        return initialized;
    }

    private void initVarMap(final File rootDir) {
        varMap = new HashMap<>();
        varMap.put(ROOT_DIR_VAR, rootDir.toString());
        if (variables != null) {
            final List<Variable> vars = variables.asList();
            for (final Variable var : vars) {
                if (var.getName().equals(ROOT_DIR_VAR)) {
                    LOG.warn("Replaced root directory '{}' with: '{}'", var.getValue(), rootDir);
                } else {
                    varMap.put(var.getName(), var.getValue());
                }
            }
            varMap = new VariableResolver(varMap).getResolved();
        }
    }

    /**
     * Initializes this object and it's childs.<br>
     * <br>
     * <b>CAUTION:</b> Elements contained in this configuration may be changed. If you serialize the object after calling this method the
     * new state will be saved.
     * 
     * @param context
     *            Current context.
     * @param rootDir
     *            Root directory that is available as variable 'rootDir'.
     * 
     * @return This instance.
     */
    public final SrcGen4JConfig init(final SrcGen4JContext context, @FileExists @IsDirectory final File rootDir) {

        Contract.requireArgNotNull("context", context);
        Contract.requireArgNotNull(ROOT_DIR_VAR, rootDir);
        FileExistsValidator.requireArgValid(ROOT_DIR_VAR, rootDir);
        IsDirectoryValidator.requireArgValid(ROOT_DIR_VAR, rootDir);

        initVarMap(rootDir);
        if (variables != null) {
            variables.init(varMap);
        }
        if (projects != null) {
            for (final Project project : projects) {
                project.init(context, this, varMap);
            }
        }
        if (replacers != null) {
            for (final Replacer replacer : replacers) {
                replacer.init(varMap);
            }
        }
        if (generators != null) {
            generators.init(context, this, varMap);
        }
        if (parsers != null) {
            parsers.init(context, this, varMap);
        }
        initialized = true;
        return this;
    }

    /**
     * Resolves the folder for a given project name and folder name.
     *
     * @param projectName
     *            Name of the target project.
     * @param folderName
     *            Name of the target folder inside the project.
     *
     * @return Folder or NULL if the project or the folder could not be found.
     */
    @Nullable
    public final Folder findFolder(final String projectName, final String folderName) {

        Contract.requireArgNotNull("projectName", projectName);
        Contract.requireArgNotNull("folderName", folderName);

        if (projects == null) {
            return null;
        }
        int idx = projects.indexOf(new Project(projectName, "dummy"));
        if (idx < 0) {
            return null;
        }
        final Project project = projects.get(idx);

        final List<Folder> folders = project.getFolders();
        if (folders == null) {
            return null;
        }
        idx = folders.indexOf(new Folder(folderName, "NotUsed"));
        if (idx < 0) {
            return null;
        }
        return folders.get(idx);

    }

    /**
     * Find all generators that are connected to a given parser.
     * 
     * @param parserName
     *            Name of the parser to return the generators for.
     * 
     * @return List of generators.
     */
    public final List<GeneratorConfig> findGeneratorsForParser(final String parserName) {
        Contract.requireArgNotNull("parserName", parserName);

        final List<GeneratorConfig> list = new ArrayList<>();
        if (generators == null) {
            return list;
        }
        final List<GeneratorConfig> gcList = generators.getList();
        if (gcList == null) {
            return list;
        }
        for (final GeneratorConfig gc : gcList) {
            if (gc.getParser().equals(parserName)) {
                list.add(gc);
            }
        }
        return list;
    }

    /**
     * Creates a new configuration with a single project and a Maven directory structure.
     * 
     * @param context
     *            Current context.
     * @param projectName
     *            Name of the one and only project.
     * @param rootDir
     *            Root directory that is available as variable 'srcgen4jRootDir'.
     * 
     * @return New initialized configuration instance.
     */
    public static SrcGen4JConfig createMavenStyleSingleProject(final SrcGen4JContext context, final String projectName,
            @FileExists @IsDirectory final File rootDir) {

        Contract.requireArgNotNull("context", context);
        Contract.requireArgNotNull(ROOT_DIR_VAR, rootDir);
        FileExistsValidator.requireArgValid(ROOT_DIR_VAR, rootDir);
        IsDirectoryValidator.requireArgValid(ROOT_DIR_VAR, rootDir);
        Contract.requireArgNotNull("projectName", projectName);

        final SrcGen4JConfig config = new SrcGen4JConfig();
        final List<Project> projects = new ArrayList<>();
        final Project project = new Project(projectName, ".");
        project.setMaven(true);
        projects.add(project);
        config.setProjects(projects);
        config.init(context, rootDir);
        return config;
    }

}
