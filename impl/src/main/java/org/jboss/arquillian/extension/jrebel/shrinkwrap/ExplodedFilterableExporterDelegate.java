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

import org.jboss.arquillian.extension.jrebel.RebelArchiveFilter;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.impl.base.exporter.ExplodedExporterDelegate;

import java.io.File;

public class ExplodedFilterableExporterDelegate extends ExplodedExporterDelegate {
    // ------------------------------ FIELDS ------------------------------

    private ArchiveFilter filter;

    // --------------------------- CONSTRUCTORS ---------------------------

    public ExplodedFilterableExporterDelegate(Archive<?> archive, File baseDirectory, String directoryName,
        ArchiveFilter filter) {
        super(archive, baseDirectory, directoryName);
        this.filter = filter;
    }

    @Override
    protected void processNode(ArchivePath path, Node node) {
        if (ArchiveHelper.isNestedArchiveOfEAR(getArchive(), node)) {
            final Archive<?> archive = ((ArchiveAsset) node.getAsset()).getArchive();
            new ExplodedFilterableExporterDelegate(archive, getResult(), path.get(),
                new RebelArchiveFilter(archive)).export();
        } else if (filter.accept(node)) {
            super.processNode(path, node);
        }
    }
}
