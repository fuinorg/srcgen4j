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
package org.fuin.srcgen4j.core.xtext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.srcgen4j.commons.ParseException;
import org.fuin.srcgen4j.commons.Parser;
import org.fuin.srcgen4j.commons.ParserConfig;
import org.fuin.srcgen4j.commons.SrcGen4JContext;
import org.fuin.srcgen4j.core.emf.AbstractEMFParser;
import org.fuin.srcgen4j.core.emf.PrimaryResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses Xtext models.
 */
public final class XtextParser extends AbstractEMFParser<XtextParserConfig> implements Parser<ResourceSet> {

    private static final Logger LOG = LoggerFactory.getLogger(XtextParser.class);

    private XtextParserConfig parserConfig;

    /**
     * Default constructor.
     */
    @SuppressWarnings("NullAway.Init") // Fields are populated by initialize() before use
    public XtextParser() {
        super(XtextParserConfig.class);
    }

    @Override
    public final void initialize(final SrcGen4JContext context, @Nullable final ParserConfig config) {

        // Xtext always needs a configuration
        if (config == null) {
            throw new ConstraintViolationException("The argument 'config' cannot be null");
        }

        this.parserConfig = getConcreteConfig(config);

        setModelDirs(parserConfig.getModelDirs());
        setFileExtensions(parserConfig.getModelExt());
        setModelResources(parserConfig.getModelResources());

        doSetup();
    }

    private void doSetup() {
        final String errorMessage = "Initializing the Xtext DSL with '" + parserConfig.getSetupClassName() + ".doSetup()' failed!";
        try {
            final Method method = parserConfig.getSetupClass().getDeclaredMethod("doSetup");
            method.invoke(parserConfig.getSetupClass(), new Object[] {});
        } catch (final SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException ex) {
            throw new RuntimeException(errorMessage, ex);
        }
    }

    @Override
    public final ResourceSet parse() throws ParseException {

        parseModel();
        // resolveProxies(); TODO Do we need to resolve cross references?

        if (isError()) {
            throw new ParseException("There was an error parsing at least one of the resources - See log for details");
        }
        if (!isModelFullyResolved()) {
            throw new ParseException("There is at least one unresolved reference - See log for details");
        }
        validate();
        return getResourceSet();

    }

    /**
     * Runs the DSL's semantic validation (the Xtext {@code @Check} rules) on the parsed models.
     * Loading a resource only reports syntax and linking problems, so without this step a model that
     * violates the DSL's own rules would be generated as if it were valid.
     * <p>
     * Only {@link PrimaryResources primary} resources are validated. Resources that were pulled in
     * merely to resolve cross references (cached dependency models, for example) belong to another
     * build and must not fail this one.
     *
     * @throws ParseException
     *             At least one model has a validation error.
     */
    private void validate() throws ParseException {

        final List<String> errors = new ArrayList<>();

        // Copy: validating a resource may load further resources into the set
        for (final Resource resource : new ArrayList<>(getResourceSet().getResources())) {
            if (!PrimaryResources.isPrimary(resource)) {
                continue;
            }
            final IResourceValidator validator = validatorFor(resource);
            if (validator == null) {
                continue;
            }
            for (final Issue issue : validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl)) {
                if (issue.getSeverity() == Severity.ERROR) {
                    errors.add(asString(issue));
                } else {
                    LOG.warn("{}", asString(issue));
                }
            }
        }

        if (!errors.isEmpty()) {
            // The messages are part of the exception because they are the only place the modeller
            // gets to see what is actually wrong with the model.
            throw new ParseException("The model is invalid:" + System.lineSeparator()
                    + String.join(System.lineSeparator(), errors));
        }

    }

    /**
     * Returns the validator the DSL registered for a given resource. The setup class registers it in
     * the {@link IResourceServiceProvider.Registry} for the file extension of the language.
     *
     * @param resource
     *            Resource to find the validator for.
     *
     * @return Validator or {@code null} if the resource does not belong to an Xtext language.
     */
    @Nullable
    private static IResourceValidator validatorFor(final Resource resource) {
        final IResourceServiceProvider provider = IResourceServiceProvider.Registry.INSTANCE
                .getResourceServiceProvider(resource.getURI());
        if (provider == null) {
            LOG.debug("No Xtext service provider for {} - not validated", resource.getURI());
            return null;
        }
        return provider.get(IResourceValidator.class);
    }

    /**
     * Renders an issue including its location.
     *
     * @param issue
     *            Issue to render.
     *
     * @return Message with file and line.
     */
    private static String asString(final Issue issue) {
        final StringBuilder sb = new StringBuilder();
        final URI uri = issue.getUriToProblem();
        if (uri != null) {
            // Without the fragment: it points to the object in the model tree, but the line does that better
            sb.append(uri.trimFragment());
        }
        if (issue.getLineNumber() != null) {
            sb.append(" line ").append(issue.getLineNumber());
        }
        if (sb.length() > 0) {
            sb.append(": ");
        }
        return sb.append(issue.getMessage()).toString();
    }

}
