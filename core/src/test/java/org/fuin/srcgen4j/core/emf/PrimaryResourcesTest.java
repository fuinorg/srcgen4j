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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PrimaryResources}.
 */
class PrimaryResourcesTest {

    private static final URI URI_A = URI.createFileURI("/tmp/a.model");

    private static final URI URI_B = URI.createFileURI("/tmp/b.model");

    @Test
    void testNoMarkerEverythingIsPrimary() {

        // PREPARE
        final ResourceSet rs = new ResourceSetImpl();
        final Resource ra = newResource(rs, URI_A);
        final Resource rb = newResource(rs, URI_B);
        final EClass oa = newObject(ra);
        final EClass ob = newObject(rb);

        // TEST & VERIFY: Without an installed marker every resource is treated as primary
        assertThat(PrimaryResources.isPrimary(ra)).isTrue();
        assertThat(PrimaryResources.isPrimary(rb)).isTrue();
        assertThat(PrimaryResources.isPrimary(oa)).isTrue();
        assertThat(PrimaryResources.isPrimary(ob)).isTrue();
    }

    @Test
    void testMarkerSelectsPrimaryResources() {

        // PREPARE
        final ResourceSet rs = new ResourceSetImpl();
        final Resource ra = newResource(rs, URI_A);
        final Resource rb = newResource(rs, URI_B);
        final EClass oa = newObject(ra);
        final EClass ob = newObject(rb);

        // TEST: Only resource A is primary
        PrimaryResources.install(rs, Set.of(URI_A));

        // VERIFY
        assertThat(PrimaryResources.isPrimary(ra)).isTrue();
        assertThat(PrimaryResources.isPrimary(oa)).isTrue();
        assertThat(PrimaryResources.isPrimary(rb)).isFalse();
        assertThat(PrimaryResources.isPrimary(ob)).isFalse();
    }

    @Test
    void testInstallReplacesPreviousMarker() {

        // PREPARE
        final ResourceSet rs = new ResourceSetImpl();
        final Resource ra = newResource(rs, URI_A);
        final Resource rb = newResource(rs, URI_B);

        // TEST: Install twice - the second call must replace the first
        PrimaryResources.install(rs, Set.of(URI_A));
        PrimaryResources.install(rs, Set.of(URI_B));

        // VERIFY: Only one marker is present and it reflects the last install
        assertThat(rs.eAdapters().stream().filter(a -> a instanceof PrimaryResources)).hasSize(1);
        assertThat(PrimaryResources.isPrimary(ra)).isFalse();
        assertThat(PrimaryResources.isPrimary(rb)).isTrue();
    }

    @Test
    void testObjectWithoutResourceIsPrimary() {

        // PREPARE: An orphan object that is not contained in any resource
        final EClass orphan = EcoreFactory.eINSTANCE.createEClass();

        // TEST & VERIFY
        assertThat(orphan.eResource()).isNull();
        assertThat(PrimaryResources.isPrimary(orphan)).isTrue();
    }

    @Test
    void testGetUris() {

        // PREPARE
        final ResourceSet rs = new ResourceSetImpl();
        PrimaryResources.install(rs, Set.of(URI_A, URI_B));
        final PrimaryResources marker = (PrimaryResources) rs.eAdapters().stream().filter(a -> a instanceof PrimaryResources).findFirst()
                .orElseThrow();

        // TEST & VERIFY
        assertThat(marker.getUris()).containsExactlyInAnyOrder(URI_A, URI_B);
    }

    private static Resource newResource(final ResourceSet rs, final URI uri) {
        final Resource resource = new ResourceImpl(uri);
        rs.getResources().add(resource);
        return resource;
    }

    private static EClass newObject(final Resource resource) {
        final EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        resource.getContents().add(eClass);
        return eClass;
    }

}
