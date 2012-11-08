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

import junit.framework.Assert;
import org.apache.commons.io.DirectoryWalker;
import org.jboss.arquillian.extension.jrebel.fakes.Fake1;
import org.jboss.arquillian.extension.jrebel.fakes.Fake2;
import org.jboss.arquillian.extension.jrebel.fakes.delta.Fake3;
import org.jboss.arquillian.extension.jrebel.fakes.delta.nested.Fake4;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RebelArchiveFilterTestCase {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void getFileAssets()
    {
        final WebArchive archive = Packager.createWebArchive();
        final Asset serializerClassAsset = archive.get("/WEB-INF/classes/" + Serializer.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final Asset fake1ClassAsset = archive.get("/WEB-INF/classes/" + Fake1.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final Asset directoryWalkerClassAsset = archive.get("/WEB-INF/classes/" + DirectoryWalker.class.getCanonicalName().replaceAll("\\.", "/") + ".class")
            .getAsset();
        final Asset fake2ClassAsset = archive.get("/WEB-INF/classes/" + Fake2.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final Asset fake3ClassAsset = archive.get("/WEB-INF/classes/" + Fake3.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final Asset fake4ClassAsset = archive.get("/WEB-INF/classes/" + Fake4.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final RebelArchiveFilter archiveFilter = new RebelArchiveFilter(archive);
        final List<Node> fileOrClassAssets = new ArrayList<Node>(archiveFilter.getFileOrClassNodes());
        Assert.assertEquals(9, fileOrClassAssets.size());
        for (Node node : fileOrClassAssets) {
            Asset asset = node.getAsset();
            if (asset instanceof FileAsset) {
                Assert.assertTrue(Packager.SAMPLE_WEB_RESOURCE.equals(asset) || Packager.SAMPLE_WEB_RESOURCE2.equals(asset));
            } else if (asset instanceof ClassAsset) {
                Assert.assertTrue(serializerClassAsset.equals(asset) || directoryWalkerClassAsset.equals(asset));
            } else if (asset instanceof ClassLoaderAsset) {
                Assert.assertTrue(
                    serializerClassAsset.equals(asset) || fake1ClassAsset.equals(asset) || fake2ClassAsset.equals(asset) || fake3ClassAsset.equals(asset)
                        || fake4ClassAsset.equals(asset));
            } else {
                Assert.assertTrue("Invalid type of asset: " + asset, false);
            }
        }

        final List<Asset> nonFileAssests = new ArrayList<Asset>(archiveFilter.getNonFileNonClassAssets());
        Assert.assertEquals(2, nonFileAssests.size());
        Assert.assertTrue(nonFileAssests.contains(EmptyAsset.INSTANCE));
    }
}
