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
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class DeploymentInterceptorTest {
    // -------------------------- OTHER METHODS --------------------------

    @Test
    public void skipEmptyExplode() throws IOException {
        final Archive<?> archive = Packager.createFullyReloadableWebArchive();
        final DeploymentInterceptor deploymentInterceptor = createDeploymentInterceptorMock();
        final File tempDirectory = deploymentInterceptor.getTempDirectory();
        FileUtils.deleteDirectory(tempDirectory);
        ShrinkWrapUtil.createTempDirectory(tempDirectory);
        final String jrebelExtRoot = tempDirectory.getPath();
        final String deploymentRoot = jrebelExtRoot
            + "/"
            + DeploymentInterceptorTest.class.getCanonicalName()
            + "/mock/fullyReloadableWebArchive.war";

        final File jrebelExtRootDir = new File(jrebelExtRoot);
        Assert.assertTrue(jrebelExtRootDir.exists());
        final File deploymentRootDir = new File(deploymentRoot);
        Assert.assertFalse(deploymentRootDir.exists());

        deploymentInterceptor.onDeploy(createDeployEventContextMock(archive),
            new TestClass(DeploymentInterceptorTest.class));

        Assert.assertFalse(deploymentRootDir.exists());
    }

    @Test
    public void testExplodedEnterpriseArchive() throws IOException {
        final Archive<?> archive = Packager.createEnterpriseArchive();
        final DeploymentInterceptor deploymentInterceptor = createDeploymentInterceptorMock();
        final File tempDirectory = deploymentInterceptor.getTempDirectory();
        FileUtils.deleteDirectory(tempDirectory);
        ShrinkWrapUtil.createTempDirectory(tempDirectory);
        final String jrebelExtRoot = tempDirectory.getPath();
        final String deploymentRoot =
            jrebelExtRoot + "/" + DeploymentInterceptorTest.class.getCanonicalName() + "/mock/test.ear";

        final File jrebelExtRootDir = new File(jrebelExtRoot);
        Assert.assertTrue(jrebelExtRootDir.exists());
        final File deploymentRootDir = new File(deploymentRoot);
        Assert.assertFalse(deploymentRootDir.exists());

        deploymentInterceptor.onDeploy(createDeployEventContextMock(archive),
            new TestClass(DeploymentInterceptorTest.class));

        Assert.assertTrue(deploymentRootDir.exists());

        final EnterpriseArchive explodedArchive = ShrinkWrap.create(WebArchive.class)
            .as(ExplodedImporter.class)
            .importDirectory(deploymentRootDir)
            .as(EnterpriseArchive.class);

        Assert.assertEquals(25, explodedArchive.getContent().size());
        Assert.assertNotNull(explodedArchive.get("/test.war/WEB-INF/beans.xml"));
        Assert.assertNotNull(explodedArchive.get("/test.war/WEB-INF/classes/rebel.xml"));
        Assert.assertNotNull(explodedArchive.get("/test.war/WEB-INF/classes/org"));
        Assert.assertNotNull(explodedArchive.get("/test.war/otherDir/sampleWebResource.html"));
        Assert.assertNull(explodedArchive.get("/test.war/WEB-INF/classes/org/arquillian"));
    }

    @Test
    public void testExplodedWebArchive() throws IOException {
        final Archive<?> archive = Packager.createWebArchive();
        final DeploymentInterceptor deploymentInterceptor = createDeploymentInterceptorMock();
        final File tempDirectory = deploymentInterceptor.getTempDirectory();
        FileUtils.deleteDirectory(tempDirectory);
        ShrinkWrapUtil.createTempDirectory(tempDirectory);
        final String jrebelExtRoot = tempDirectory.getPath();
        final String deploymentRoot =
            jrebelExtRoot + "/" + DeploymentInterceptorTest.class.getCanonicalName() + "/mock/test.war";

        final File jrebelExtRootDir = new File(jrebelExtRoot);
        Assert.assertTrue(jrebelExtRootDir.exists());
        final File deploymentRootDir = new File(deploymentRoot);
        Assert.assertFalse(deploymentRootDir.exists());

        deploymentInterceptor.onDeploy(createDeployEventContextMock(archive),
            new TestClass(DeploymentInterceptorTest.class));

        Assert.assertTrue(deploymentRootDir.exists());

        final WebArchive explodedArchive = ShrinkWrap.create(WebArchive.class)
            .as(ExplodedImporter.class)
            .importDirectory(deploymentRootDir)
            .as(WebArchive.class);

        Assert.assertEquals(24, explodedArchive.getContent().size());
        Assert.assertNotNull(explodedArchive.get("/WEB-INF/beans.xml"));
        Assert.assertNotNull(explodedArchive.get("/WEB-INF/classes/rebel.xml"));
        Assert.assertNotNull(explodedArchive.get("/WEB-INF/classes/org"));
        Assert.assertNotNull(explodedArchive.get("/otherDir/sampleWebResource.html"));
        Assert.assertNull(explodedArchive.get("/WEB-INF/classes/org/arquillian"));
    }

    private EventContext<DeployDeployment> createDeployEventContextMock(Archive<?> archive) {
        final Container containerMock = Mockito.mock(Container.class);
        Mockito.when(containerMock.getName()).thenReturn("mock");
        final DeploymentDescription deploymentDescription = new DeploymentDescription("test.war", archive);
        deploymentDescription.setTestableArchive(archive);
        final DeployDeployment event = new DeployDeployment(containerMock, new Deployment(deploymentDescription));
        @SuppressWarnings("unchecked") final EventContext<DeployDeployment> eventContextMock =
            Mockito.mock(EventContext.class);
        Mockito.when(eventContextMock.getEvent()).thenReturn(event);
        return eventContextMock;
    }

    private DeploymentInterceptor createDeploymentInterceptorMock() {
        try {
            final DeploymentInterceptor deploymentInterceptor = new DeploymentInterceptor();
            final Field eventField = DeploymentInterceptor.class.getDeclaredField("event");
            eventField.setAccessible(true);
            eventField.set(deploymentInterceptor, Mockito.mock(Event.class));
            eventField.setAccessible(false);
            final Field protocolMetaDataField = DeploymentInterceptor.class.getDeclaredField("protocolMetaData");
            protocolMetaDataField.setAccessible(true);
            final InstanceProducer instanceProducer = Mockito.mock(InstanceProducer.class);
            Mockito.when(instanceProducer.get()).thenReturn(Mockito.mock(ProtocolMetaData.class));
            protocolMetaDataField.set(deploymentInterceptor, instanceProducer);
            protocolMetaDataField.setAccessible(false);
            return deploymentInterceptor;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Cannot create DeploymentInterceptor mock", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot create DeploymentInterceptor mock", e);
        }
    }
}
