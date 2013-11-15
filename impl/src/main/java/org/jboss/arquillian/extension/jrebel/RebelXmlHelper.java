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

import org.jboss.arquillian.extension.jrebel.shrinkwrap.ArchiveHelper;
import org.jboss.arquillian.extension.jrebel.shrinkwrap.AssetHelper;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RebelXmlHelper {
// ------------------------------ FIELDS ------------------------------

    private static final FileFilter DIRECTORY_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname)
        {
            return pathname.isDirectory();
        }
    };

// -------------------------- STATIC METHODS --------------------------

    public static String createRebelXML(Archive<?> archive, String path)
    {
        StringBuilder contents = new StringBuilder();
        final RebelArchiveFilter archiveFilter = new RebelArchiveFilter(archive);
        final List<Node> fileNodes = new ArrayList<Node>();
        final StringBuilder includes = new StringBuilder();
        for (Node node : archiveFilter.getFileOrClassNodes()) {
            final Asset asset = node.getAsset();
            if (asset instanceof ClassAsset) {
                final String className = AssetHelper.getClass((ClassAsset) asset).getCanonicalName().replaceAll("\\.", "/");
                includes.append("\n\t\t\t<include name=\"").append(className).append(".class\"/>");
                includes.append("\n\t\t\t<include name=\"").append(className).append("$*.class\"/>");
            } else if (asset instanceof ClassLoaderAsset) {
                final String resourceName = AssetHelper.getResourceName((ClassLoaderAsset) asset);
                includes.append("\n\t\t\t<include name=\"").append(resourceName).append("\"/>");
                if (resourceName.endsWith(".class")) {
                    includes.append("\n\t\t\t<include name=\"").append(resourceName.replaceAll("\\.class$", "\\$*.class")).append("\"/>");
                }
            } else if (asset instanceof FileAsset) {
                fileNodes.add(node);
            }
        }
        final String targetDirectory;
        try {
            targetDirectory = new File("target").getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String explodedClassDirectory;
        if (ArchiveHelper.isWebArchive(archive)) {
            explodedClassDirectory = path + File.separator + "WEB-INF" + File.separator + "classes";
        } else {
            explodedClassDirectory = path;
        }
        contents.append("\n\t<classpath>")
            .append("\n\t\t<dir name=\"")
            .append(targetDirectory)
            .append("/classes\">")
            .append(includes)
            .append("\n\t\t</dir>")
            .append("\n\t\t<dir name=\"")
            .append(targetDirectory)
            .append("/test-classes\">")
            .append(includes)
            .append("\n\t\t</dir>")
            .append("\n\t\t<dir name=\"")
            .append(explodedClassDirectory)
            .append("\"/>")
            .append("\n\t</classpath>\n");

        if (ArchiveHelper.isWebArchive(archive)) {
            contents.append("\n\t<web>");
            contents.append("\n\t\t<link target=\"/\">");
            for (Map.Entry<String, List<String>> entry : RebelXmlHelper.rootize(fileNodes).entrySet()) {
                contents.append("\n\t\t\t<dir name=\"").append(entry.getKey()).append("\">");
                for (String asset : entry.getValue()) {
                    contents.append("\n\t\t\t\t<include name=\"").append(asset).append("\"/>");
                }
                contents.append("\n\t\t\t</dir>");
            }
            contents.append("\n\t\t\t<dir name=\"").append(path).append("\"/>").append("\n\t\t</link>\n\t</web>\n");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<application\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xmlns=\"http://www.zeroturnaround.com\"\n" +
            "  xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">\n" +
            contents +
            "\n</application>";
    }

    /**
     * Creates map of root path and sub paths of resources contained in that root path.
     *
     * @param nodes nodes holding FileAssets
     *
     * @return rootized map
     */
    private static Map<String, List<String>> rootize(Collection<Node> nodes)
    {
        final Rootizer rootizer = new Rootizer();
        final HashMap<String, List<String>> rootizedAssets = new HashMap<String, List<String>>();
        for (Node node : nodes) {
            final Rootizer.RootizedPath rootizedPath = rootizer.rootize(node);
            if (rootizedPath != null) {
                List<String> fileAssets = rootizedAssets.get(rootizedPath.getRoot());
                if (fileAssets == null) {
                    fileAssets = new ArrayList<String>();
                    rootizedAssets.put(rootizedPath.getRoot(), fileAssets);
                }
            }
        }
        return rootizedAssets;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private RebelXmlHelper()
    {
    }

// -------------------------- INNER CLASSES --------------------------

    public static class Rootizer {
// ------------------------------ FIELDS ------------------------------

        private final List<String> rootPaths = new ArrayList<String>();

// --------------------------- CONSTRUCTORS ---------------------------

        public Rootizer()
        {
            final File mainSources = new File("src/main");

            if (mainSources.exists()) {
                for (File file : mainSources.listFiles(DIRECTORY_FILTER)) {
                    rootPaths.add(file.getAbsolutePath());
                }
            }
            final File testSources = new File("src/test");
            if (testSources.exists()) {
                for (File file : testSources.listFiles(DIRECTORY_FILTER)) {
                    rootPaths.add(file.getAbsolutePath());
                }
            }
            rootPaths.add(mainSources.getAbsolutePath());
            rootPaths.add(testSources.getAbsolutePath());
            rootPaths.add(new File("src").getAbsolutePath());
            rootPaths.add(new File("target").getAbsolutePath());
        }

// -------------------------- OTHER METHODS --------------------------

        public RootizedPath rootize(Node node)
        {
            final Asset asset = node.getAsset();
            if (!(asset instanceof FileAsset)) {
                return null;
            }
            FileAsset fileAsset = (FileAsset) asset;
            final File file = AssetHelper.getFile(fileAsset);
            final String parentPath = file.getParentFile().getAbsolutePath();
            for (String rootPath : rootPaths) {
                if (parentPath.startsWith(rootPath)) {
                    final String subPath = file.getAbsolutePath().substring(rootPath.length());
                    if (subPath.equals(node.getPath().get())) {
                        return new RootizedPath(parentPath, subPath);
                    }
                    break;
                }
            }
            return null;
        }

// -------------------------- INNER CLASSES --------------------------

        public static class RootizedPath {
// ------------------------------ FIELDS ------------------------------

            private final String path;

            private final String root;

// --------------------------- CONSTRUCTORS ---------------------------

            public RootizedPath(String root, String path)
            {
                this.root = root;
                this.path = path;
            }

// --------------------- GETTER / SETTER METHODS ---------------------

            public String getPath()
            {
                return path;
            }

            public String getRoot()
            {
                return root;
            }

// ------------------------ CANONICAL METHODS ------------------------

            @Override
            public String toString()
            {
                return "RootizedPath{" +
                    "path='" + path + '\'' +
                    ", root='" + root + '\'' +
                    '}';
            }
        }
    }
}
