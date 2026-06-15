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
package org.fuin.srcgen4j.core.emf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.jspecify.annotations.Nullable;

/**
 * Marks the set of "primary" resources of a {@link ResourceSet} - those that were explicitly requested for parsing (the configured model
 * directories and model resources). Resources that are pulled into the resource set later on - for example dependency models loaded lazily
 * while resolving cross references - are <i>not</i> primary.
 * <p>
 * The marker is attached to the {@link ResourceSet} as an EMF adapter so it travels together with the model from the parser to the
 * generator. If no marker is installed, every resource is considered primary (backward compatible default).
 */
public final class PrimaryResources extends AdapterImpl {

    /** Key that may be used to store the set of primary URIs in a generator context map. */
    public static final String CONTEXT_KEY = PrimaryResources.class.getName();

    private final Set<URI> uris;

    private PrimaryResources(final Set<URI> uris) {
        super();
        this.uris = new HashSet<>(uris);
    }

    /**
     * Returns the URIs of all primary resources.
     *
     * @return Unmodifiable set of primary resource URIs.
     */
    public Set<URI> getUris() {
        return Collections.unmodifiableSet(uris);
    }

    @Override
    public boolean isAdapterForType(@Nullable final Object type) {
        return type == PrimaryResources.class;
    }

    /**
     * Installs (or replaces) the primary resource marker on the given resource set.
     *
     * @param resourceSet
     *            Resource set to mark.
     * @param uris
     *            URIs of the resources that are considered primary.
     */
    public static void install(final ResourceSet resourceSet, final Set<URI> uris) {
        final PrimaryResources existing = find(resourceSet);
        if (existing != null) {
            resourceSet.eAdapters().remove(existing);
        }
        resourceSet.eAdapters().add(new PrimaryResources(uris));
    }

    @Nullable
    private static PrimaryResources find(final ResourceSet resourceSet) {
        for (final Adapter adapter : resourceSet.eAdapters()) {
            if (adapter instanceof PrimaryResources) {
                return (PrimaryResources) adapter;
            }
        }
        return null;
    }

    /**
     * Determines if the given object belongs to a primary resource. An object without a resource, a resource without a resource set, or a
     * resource set without an installed marker is treated as primary.
     *
     * @param obj
     *            Object to test.
     *
     * @return {@code true} if the object is part of a primary resource.
     */
    public static boolean isPrimary(final EObject obj) {
        final Resource resource = obj.eResource();
        if (resource == null) {
            return true;
        }
        return isPrimary(resource);
    }

    /**
     * Determines if the given resource is primary. A resource without a resource set, or a resource set without an installed marker is
     * treated as primary.
     *
     * @param resource
     *            Resource to test.
     *
     * @return {@code true} if the resource is primary.
     */
    public static boolean isPrimary(final Resource resource) {
        final ResourceSet resourceSet = resource.getResourceSet();
        if (resourceSet == null) {
            return true;
        }
        final PrimaryResources marker = find(resourceSet);
        if (marker == null) {
            return true;
        }
        return marker.uris.contains(resource.getURI());
    }

}
