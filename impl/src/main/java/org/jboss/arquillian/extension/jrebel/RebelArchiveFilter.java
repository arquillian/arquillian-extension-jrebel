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
package org.jboss.arquillian.extension.jrebel;

import org.jboss.arquillian.extension.jrebel.shrinkwrap.ArchiveFilter;
import org.jboss.arquillian.extension.jrebel.shrinkwrap.ArchiveHelper;
import org.jboss.arquillian.extension.jrebel.shrinkwrap.AssetHelper;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RebelArchiveFilter implements ArchiveFilter {
// ------------------------------ FIELDS ------------------------------

    private Archive<?> archive;

    private Map<ArchivePath, Asset> fileOrClassAssets;

    private Map<ArchivePath, Asset> nonFileNonClassAssets;

// --------------------------- CONSTRUCTORS ---------------------------

    public RebelArchiveFilter(Archive<?> archive)
    {
        this.archive = archive;
        filter();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ArchiveFilter ---------------------

    @Override
    public boolean accept(Node node)
    {
        if (node.getAsset() == null) {
            return !isEmpty(node);
        } else {
            return !fileOrClassAssets.containsKey(node.getPath());
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public Collection<Asset> getFileOrClassAssets()
    {
        return fileOrClassAssets.values();
    }

    public Collection<Asset> getNonFileNonClassAssets()
    {
        return nonFileNonClassAssets.values();
    }

    private void filter()
    {
        fileOrClassAssets = new HashMap<ArchivePath, Asset>();
        nonFileNonClassAssets = new HashMap<ArchivePath, Asset>();
        Node node;
        Asset asset;
        for (Map.Entry<ArchivePath, Node> entry : archive.getContent().entrySet()) {
            node = entry.getValue();
            asset = node.getAsset();
            if (asset instanceof FileAsset || asset instanceof ClassAsset || (asset instanceof ClassLoaderAsset && isProperlyNestedClass(
                (ClassLoaderAsset) asset))) {
                fileOrClassAssets.put(entry.getKey(), asset);
            } else if (!ArchiveHelper.isNestedArchiveOfEAR(archive, node) && asset != null) {
                nonFileNonClassAssets.put(entry.getKey(), asset);
            }
        }
    }

    private boolean isEmpty(Node node)
    {
        if (node.getAsset() == null) {
            for (Node child : node.getChildren()) {
                if (!isEmpty(child)) {
                    return false;
                }
            }
            return true;
        } else {
            return fileOrClassAssets.containsKey(node.getPath());
        }
    }

    private boolean isProperlyNestedClass(ClassLoaderAsset asset)
    {
        final ClassLoader classLoader = AssetHelper.getClassLoader(asset);
        final String resourceName = AssetHelper.getResourceName(asset);
        final URL resourceURL = classLoader.getResource(resourceName);
        if (resourceURL == null) {
            return false;
        }
        if (!"file".equals(resourceURL.getProtocol())) {
            return false;
        }
        final String cwd = System.getProperty("user.dir");
        final String resourcePath = resourceURL.getFile();
        return resourcePath != null && resourcePath.startsWith(cwd);
    }
}
