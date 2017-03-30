/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.extension.jrebel.shrinkwrap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.impl.base.Validate;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExplodedFilterableExporterImpl extends AssignableBase<Archive<?>> implements ExplodedFilterableExporter {
    // ------------------------------ FIELDS ------------------------------

    private static final Logger log = Logger.getLogger(ExplodedFilterableExporterImpl.class.getName());

    // --------------------------- CONSTRUCTORS ---------------------------

    public ExplodedFilterableExporterImpl(final Archive<?> archive) {
        super(archive);
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface ExplodedFilterableExporter ---------------------

    @Override
    public File exportExploded(final File baseDirectory, ArchiveFilter filter) {
        return exportExploded(baseDirectory, this.getArchive().getName(), filter);
    }

    @Override
    public File exportExploded(File baseDirectory, String directoryName, ArchiveFilter filter) {
        final Archive<?> archive = this.getArchive();
        Validate.notNull(archive, "No archive provided");
        Validate.notNull(baseDirectory, "No baseDirectory provided");

        // Directory must exist
        if (!baseDirectory.exists()) {
            throw new IllegalArgumentException("Parent directory does not exist");
        }
        // Must be a directory
        if (!baseDirectory.isDirectory()) {
            throw new IllegalArgumentException("Provided parent directory is not a valid directory");
        }

        // Get the export delegate
        final ExplodedFilterableExporterDelegate exporterDelegate =
            new ExplodedFilterableExporterDelegate(archive, baseDirectory, directoryName, filter);

        // Run the export and get the result
        final File explodedDirectory = exporterDelegate.export();

        if (log.isLoggable(Level.FINE)) {
            log.fine("Created Exploded Archive: " + explodedDirectory.getAbsolutePath());
        }
        // Return the exploded dir
        return explodedDirectory;
    }
}
