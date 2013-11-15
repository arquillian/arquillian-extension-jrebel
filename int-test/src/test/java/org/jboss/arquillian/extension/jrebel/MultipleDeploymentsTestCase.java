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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class MultipleDeploymentsTestCase {
// ------------------------------ FIELDS ------------------------------

    private static final String OTHER_WAR_WITH_INJECTABLE_ARTIFACT = "OtherWarWithInjectableArtifact.war";

    private static final String WAR_WITH_INJECTABLE_ARTIFACT = "WarWithInjectableArtifact.war";

    @Inject
    InjectableArtifact injectableArtifact;

// -------------------------- STATIC METHODS --------------------------

    @Deployment(name = WAR_WITH_INJECTABLE_ARTIFACT)
    public static WebArchive createFirstArchive()
    {
        return Packager.warWithInjectableArtifact(WAR_WITH_INJECTABLE_ARTIFACT);
    }

    @Deployment(name = OTHER_WAR_WITH_INJECTABLE_ARTIFACT)
    public static WebArchive createSecondArchive()
    {
        return Packager.otherWarWithInjectableArtifact();
    }

// -------------------------- OTHER METHODS --------------------------

    @Test
    @OperateOnDeployment(WAR_WITH_INJECTABLE_ARTIFACT)
    public void injectableArtifactAvailable() throws Exception
    {
        /**
         * Run tests once, then modify this method and run tests again.
         * Notice that unless you run "mvn clean" the package is not redeployed between "mvn test" runs.
         */
        System.out.println(injectableArtifact);
        Assert.assertNotNull(injectableArtifact);
    }

    @Test
    @OperateOnDeployment(OTHER_WAR_WITH_INJECTABLE_ARTIFACT)
    public void injectableArtifactAvailable2() throws Exception
    {
        Assert.assertNotNull(injectableArtifact);
    }
}
