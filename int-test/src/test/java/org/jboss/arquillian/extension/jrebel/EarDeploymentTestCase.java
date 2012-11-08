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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class EarDeploymentTestCase {
// ------------------------------ FIELDS ------------------------------

    @Inject
    EJBBean EJBBean;

    @Inject
    InjectableArtifact injectableArtifact;

// -------------------------- STATIC METHODS --------------------------

    @Deployment
    public static EnterpriseArchive createEar()
    {
        final JavaArchive ejbJar = Packager.ejbJar();
        /**
         * If we'd stuff test class into ejbJar then we wouldn't be able to inject stuff from webArchive
         */
//        ejbJar.addClass(EARDeploymentTestCase.class);
        final WebArchive webArchive = Packager.warWithInjectableArtifact();
        webArchive.addClass(EarDeploymentTestCase.class);
        /**
         * After first test run look at http://localhost:8080/withInjectableArtifact/dynamic.html.
         * It should return 404.
         * Then uncomment first line and run tests, now under the URL should be empty page.
         * Finaly uncomment second line, run tests and refresh the page. It should say "Hello".
         */
//        webArchive.addAsWebResource(EmptyAsset.INSTANCE, ArchivePaths.create("dynamic.html"));
//        webArchive.addAsWebResource(new StringAsset("Hello"), ArchivePaths.create("dynamic.html"));
        return ShrinkWrap.create(EnterpriseArchive.class, "jrebel-test.ear").addAsModule(ejbJar).addAsModule(webArchive);
    }

// -------------------------- OTHER METHODS --------------------------

    @Test
    public void shouldBeAbleToiChange() throws Exception
    {
        /**
         * Run tests once, then modify this method and run tests again.
         * Notice that unless you run "mvn clean" the package is not redeployed between "mvn test" runs.
         */
        Assert.assertNotNull(injectableArtifact);
        Assert.assertNotNull(EJBBean);
        System.out.println(injectableArtifact);
        System.out.println(EJBBean);
        /**
         * Run EARDeploymentTestCase once then uncomment following lines as well as corresponding methods in EJBBean and InjectableArtifact
         * and you will see that it gets hot deployed.
         */
//        System.out.println(EJBBean.foo());
//        System.out.println(injectableArtifact.bar());
    }
}
