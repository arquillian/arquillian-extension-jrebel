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
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;

public final class ArchiveHelper {
    // -------------------------- STATIC METHODS --------------------------

    public static boolean isEarArchive(Archive<?> archive) {
        return archive.getName().endsWith(".ear");
    }

    public static boolean isNestedArchiveOfEAR(Archive<?> archive, Node node) {
        return ArchiveHelper.isEarArchive(archive) && isTopLevelNode(node) && node.getAsset() instanceof ArchiveAsset;
    }

    public static boolean isTopLevelNode(Node node) {
        final ArchivePath parent = node.getPath().getParent();
        return parent == null || "/".equals(parent.get());
    }

    public static boolean isWebArchive(Archive<?> archive) {
        return archive.getName().endsWith(".war");
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    private ArchiveHelper() {
    }
}
