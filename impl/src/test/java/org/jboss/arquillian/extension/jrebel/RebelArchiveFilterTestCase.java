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
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
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
        final Asset classAsset = archive.get("/WEB-INF/classes/" + Serializer.class.getCanonicalName().replaceAll("\\.", "/") + ".class").getAsset();
        final RebelArchiveFilter archiveFilter = new RebelArchiveFilter(archive);
        final List<Asset> fileOrClassAssets = new ArrayList<Asset>(archiveFilter.getFileOrClassAssets());
        Assert.assertEquals(3, fileOrClassAssets.size());
        for (Asset asset : fileOrClassAssets) {
            if (asset instanceof FileAsset) {
                Assert.assertTrue(Packager.SAMPLE_WEB_RESOURCE.equals(asset) || Packager.SAMPLE_WEB_RESOURCE2.equals(asset));
            } else if (asset instanceof ClassAsset) {
                Assert.assertEquals(classAsset, asset);
            } else {
                Assert.assertTrue("Invalid type of asset: " + asset, false);
            }
        }
        final List<Asset> nonFileAssests = new ArrayList<Asset>(archiveFilter.getNonFileNonClassAssets());
        Assert.assertEquals(1, nonFileAssests.size());
        Assert.assertEquals(EmptyAsset.INSTANCE, nonFileAssests.get(0));
    }
}
