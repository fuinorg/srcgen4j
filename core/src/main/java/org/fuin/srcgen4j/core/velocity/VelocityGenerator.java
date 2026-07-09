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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.fuin.srcgen4j.commons.GeneratedArtifact;
import org.jspecify.annotations.Nullable;
import org.fuin.srcgen4j.commons.GenerateException;
import org.fuin.srcgen4j.core.base.AbstractGenerator;
import org.fuin.srcgen4j.core.base.GeneratedFile;
import org.fuin.utils4j.Utils4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base generator that uses velocity templates for generation.
 * 
 * @param <MODEL>
 *            Type of the model.
 */
public abstract class VelocityGenerator<MODEL> extends AbstractGenerator<MODEL, VelocityGeneratorConfig> {

    /** Key for the location of the template files. */
    public static final String TEMPLATE_DIR_KEY = "templateDir";

    private static final Logger LOG = LoggerFactory.getLogger(VelocityGenerator.class);

    private VelocityEngine ve;

    @Nullable
    private File templateDir;

    /**
     * Default constructor.
     */
    @SuppressWarnings("NullAway.Init") // Fields are populated by generate() before use
    protected VelocityGenerator() {
        super();
    }

    /**
     * Returns an initialized velocity engine.
     *
     * @return Engine - Never NULL after {@link #generate(boolean)} was called.
     */
    protected final VelocityEngine getVE() {
        return ve;
    }

    /**
     * Returns the template directory.
     *
     * @return Source directory.
     */
    @Nullable
    public final File getTemplateDir() {
        return templateDir;
    }

    private static VelocityEngine createVelocityEngine(@Nullable final File templateDir) {
        final VelocityEngine ve = new VelocityEngine();
        if (templateDir == null) {
            ve.addProperty("resource.loader", "class");
        } else {
            ve.addProperty("resource.loader", "file, class");
            ve.addProperty("file.resource.loader.class", FileResourceLoader.class.getName());
            ve.addProperty("file.resource.loader.path", templateDir.toString());
        }
        ve.addProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        return ve;
    }

    /**
     * Merges the template and context into a file. If the directory of the file does not exists, the full directory path to it will be
     * created.
     * 
     * @param context
     *            Context to use.
     * @param artifactName
     *            Unique name of the generated artifact.
     * @param templateName
     *            Name of the template to use.
     * @param filename
     *            Filename relative to the target directory.
     * @param module
     *            Name of the target module.
     * @param folder
     *            Name of the target folder inside the module.
     *
     * @throws GenerateException
     *             Error merging the template
     */
    protected final void merge(final VelocityContext context, final String artifactName, final String templateName, final String filename,
            final String module, final String folder) throws GenerateException {

        final GeneratedFile genFile = getTargetFile(new GeneratedArtifact(artifactName, filename, new byte[] {}, module, folder),
                templateName);
        if (genFile.isSkip()) {
            LOG.debug("Omitted already existing file: {} [{}]", genFile, templateName);
        } else {
            LOG.debug("Start merging velocity template: {} [{}]", genFile, templateName);
            // Merge content
            try {
                try (final Writer writer = new FileWriter(genFile.getTmpFile())) {
                    final Template template = ve.getTemplate(templateName);
                    template.merge(context, writer);
                }
                genFile.persist();

            } catch (final IOException ex) {
                throw new GenerateException("Error merging template '" + templateName + "' to '" + filename + "'!", ex);
            }
        }

    }

    @Override
    public final Class<VelocityGeneratorConfig> getSpecificConfigClass() {
        return VelocityGeneratorConfig.class;
    }

    @Override
    public final void generate(final boolean incremental) throws GenerateException {
        final VelocityGeneratorConfig specificConfig = getSpecificConfig();
        if (specificConfig == null) {
            throw new IllegalStateException("Specific configuration was not set");
        }
        this.templateDir = Utils4J.getCanonicalFile(specificConfig.getTemplateDir());
        this.ve = createVelocityEngine(templateDir);

        LOG.debug("Template directory: {}", templateDir);

        generateIntern();
    }

    /**
     * Generates the files from velocity templates. The method {@link #getVE()} can be used to get a ready to use velocity engine that
     * points to the template directory.
     * 
     * @throws GenerateException
     *             Error generating the files.
     */
    protected abstract void generateIntern() throws GenerateException;

}
