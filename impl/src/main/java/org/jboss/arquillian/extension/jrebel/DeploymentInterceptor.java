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

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DeploymentInterceptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentInterceptor {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = Logger.getLogger(DeploymentInterceptor.class.getName());

    @Inject
    @DeploymentScoped
    private InstanceProducer<DeploymentDescription> deploymentDescriptionProducer;

    @Inject
    @DeploymentScoped
    private InstanceProducer<Deployment> deploymentProducer;

    @Inject
    private Event<DeploymentEvent> event;

    private boolean forcedUndeployment;

    @Inject
    @DeploymentScoped
    private InstanceProducer<ProtocolMetaData> protocolMetaData;

    private final File tempDirectory;

// --------------------------- CONSTRUCTORS ---------------------------

    public DeploymentInterceptor()
    {
        tempDirectory = ShrinkWrapUtil.createTempDirectory();
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDeploy(@Observes(precedence = -1) EventContext<DeployDeployment> eventContext, TestClass testClass)
    {
        final Deployment deployment = eventContext.getEvent().getDeployment();
        Archive<?> testableArchive = deployment.getDescription().getTestableArchive();
        Archive<?> archive = deployment.getDescription().getArchive();
        final File explodedDeploymentDirectory = new File(
            tempDirectory + "/" + testClass.getJavaClass().getCanonicalName() + "/" + eventContext.getEvent().getContainer().getName());
        File exportPath = new File(explodedDeploymentDirectory, testableArchive.getName());
        File metaDataFile = new File(explodedDeploymentDirectory, testableArchive.getName() + ".meta");
        boolean alreadyDeployed = exportPath.exists() && metaDataFile.exists();
        //noinspection ResultOfMethodCallIgnored
        exportPath.mkdirs();

        if (!alreadyDeployed) {
            processArchiveAndProceedWithDeployment(eventContext, deployment, testableArchive, archive, explodedDeploymentDirectory, exportPath, metaDataFile);
        } else {
            testableArchive.as(ExplodedExporter.class).exportExploded(explodedDeploymentDirectory);
            ProtocolMetaData metaData = new ProtocolMetaData();
            SerializableHttpContextData serializableHttpContextData = Serializer.toObject(SerializableHttpContextData.class, metaDataFile);
            metaData.addContext(serializableHttpContextData.toHTTPContext());
            protocolMetaData.set(metaData);
            deployment.deployed();
            deploymentProducer.set(deployment);
            deploymentDescriptionProducer.set(deployment.getDescription());
        }
    }

    public void onUnDeploy(@Observes EventContext<UnDeployDeployment> eventContext)
    {
        if (forcedUndeployment) {
            eventContext.proceed();
        }
    }

    /**
     * If no rebel.xml file found in archive in appropriate locations then
     * rebel.xml is generated.
     *
     * @param archive  archive to inspect and add rebel.xml to
     * @param rootPath path to use when calculating exploded directory path
     */
    private void addRebelXmlIfNeeded(Archive<?> archive, String rootPath)
    {
        final String path = rootPath + "/" + archive.getName();
        if (archive instanceof WebArchive) {
            final String archivePath = "WEB-INF/classes/rebel.xml";
            if (archive.get(archivePath) == null) {
                archive.add(new StringAsset(createRebelXML(path, true)), archivePath);
            }
        } else {
            final String archivePath = "rebel.xml";
            if (archive.get(archivePath) == null) {
                archive.add(new StringAsset(createRebelXML(path, false)), archivePath);
            }
        }
    }

    private String createRebelXML(String path, boolean forWebArchive)
    {
        String contents;
        if (forWebArchive) {
            contents = "<war dir=\"" + path + "\"/>\n";
        } else {
            contents = "<classpath><dir name=\"" + path + "\"/></classpath>";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<application\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xmlns=\"http://www.zeroturnaround.com\"\n" +
            "  xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">\n" +
            contents +
            "</application>";
    }

    private void processArchiveAndProceedWithDeployment(EventContext<DeployDeployment> eventContext, Deployment deployment, Archive<?> testableArchive,
                                                        Archive<?> archive, File explodedDeploymentDirectory, File exportPath, File metaDataFile)
    {
        forcedUndeployment = true;
        try {
            event.fire(new UnDeployDeployment(eventContext.getEvent().getContainer(), deployment));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot undeploy " + deployment.getDescription().getName(), e);
        } finally {
            forcedUndeployment = false;
        }
        final String mainArchivePath = exportPath.getAbsolutePath();
        if (!testableArchive.equals(archive)) {
            for (Node node : testableArchive.getContent().values()) {
                final Asset asset = node.getAsset();
                if (asset instanceof ArchiveAsset) {
                    if (archive.equals(((ArchiveAsset) asset).getArchive())) {
                        addRebelXmlIfNeeded(archive, mainArchivePath + node.getPath().getParent().get());
                        break;
                    }
                }
            }
        }
        if (testableArchive instanceof WebArchive) {
            addRebelXmlIfNeeded(testableArchive, explodedDeploymentDirectory.getAbsolutePath());
        } else if (testableArchive instanceof EnterpriseArchive) {
            final EnterpriseArchive enterpriseArchive = (EnterpriseArchive) testableArchive;
            final Set<Node> rootChildren = enterpriseArchive.get("/").getChildren();
            for (Node node : rootChildren) {
                final Asset asset = node.getAsset();
                if (asset == null) {
                    continue;
                }
                if (asset instanceof ArchiveAsset) {
                    addRebelXmlIfNeeded(((ArchiveAsset) asset).getArchive(), mainArchivePath);
                }
            }
        }
        testableArchive.as(ExplodedExporter.class).exportExploded(explodedDeploymentDirectory);
        eventContext.proceed();

        HTTPContext httpContext = protocolMetaData.get().getContext(HTTPContext.class);
        if (httpContext != null) {
            Serializer.toStream(new SerializableHttpContextData(httpContext), metaDataFile);
        }
    }
}
