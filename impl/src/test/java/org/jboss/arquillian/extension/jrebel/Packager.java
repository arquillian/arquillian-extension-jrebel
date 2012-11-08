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

import org.apache.commons.io.DirectoryWalker;
import org.jboss.arquillian.extension.jrebel.fakes.Fake1;
import org.jboss.arquillian.extension.jrebel.fakes.delta.Fake3;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;

public final class Packager {
// ------------------------------ FIELDS ------------------------------

    public static final FileAsset SAMPLE_WEB_RESOURCE = new FileAsset(new File("src/test/resources/sampleWebResource.html"));

    public static final FileAsset SAMPLE_WEB_RESOURCE2 = new FileAsset(new File("src/test/webapp/sampleWebResource2.html"));

// -------------------------- STATIC METHODS --------------------------

    public static EnterpriseArchive createEnterpriseArchive()
    {
        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear").addAsModule(createWebArchive());
    }

    public static WebArchive createFullyReloadableWebArchive()
    {
        return ShrinkWrap.create(WebArchive.class, "fullyReloadableWebArchive.war")
            .addPackages(true, Fake1.class.getPackage())
            .addAsWebResource(SAMPLE_WEB_RESOURCE, ArchivePaths.create("sampleWebResource.html"))
            .addAsWebResource(SAMPLE_WEB_RESOURCE2, ArchivePaths.create("sampleWebResource2.html"));
    }

    public static WebArchive createWebArchive()
    {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(Serializer.class)
            .addClass(DirectoryWalker.class)
            .addPackage(Fake1.class.getPackage())
            .addPackages(true, Fake3.class.getPackage())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebResource(SAMPLE_WEB_RESOURCE, ArchivePaths.create("sampleWebResource.html"))
            .addAsWebResource(SAMPLE_WEB_RESOURCE, ArchivePaths.create("otherDir/sampleWebResource.html"))
            .addAsWebResource(SAMPLE_WEB_RESOURCE2, ArchivePaths.create("sampleWebResource2.html"));
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private Packager()
    {
    }
}
