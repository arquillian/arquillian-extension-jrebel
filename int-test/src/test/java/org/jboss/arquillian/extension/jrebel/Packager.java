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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;

public final class Packager {
// -------------------------- STATIC METHODS --------------------------

    public static JavaArchive ejbJar()
    {
        return ShrinkWrap.create(JavaArchive.class, "ejb.jar").addAsResource(EmptyAsset.INSTANCE, "beans.xml").addClass(EJBBean.class);
    }

    public static WebArchive otherWarWithInjectableArtifact()
    {
        return ShrinkWrap.create(WebArchive.class, "otherWithInjectableArtifact.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClass(InjectableArtifact.class);
    }

    public static WebArchive warWithInjectableArtifact()
    {
        return ShrinkWrap.create(WebArchive.class, "withInjectableArtifact.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebResource(new File("src/main/webapp/sampleWebResource.html"), "sampleWebResource.html")
            .addClass(InjectableArtifact.class);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private Packager()
    {
    }
}
