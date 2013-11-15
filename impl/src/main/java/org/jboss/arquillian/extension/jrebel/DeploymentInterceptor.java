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
import org.jboss.arquillian.extension.jrebel.shrinkwrap.ArchiveHelper;
import org.jboss.arquillian.extension.jrebel.shrinkwrap.ExplodedFilterableExporter;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

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

    private File tempDirectory = new File("target" + File.separator + "jrebel-temp");

    public File getTempDirectory()
    {
        return tempDirectory;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onDeploy(@Observes(precedence = -1) EventContext<DeployDeployment> eventContext, TestClass testClass)
    {
        tempDirectory = ShrinkWrapUtil.createTempDirectory(getTempDirectory());
        final DeployDeployment event = eventContext.getEvent();
        final Deployment deployment = event.getDeployment();
        Archive<?> testableArchive = deployment.getDescription().getTestableArchive();
        Archive<?> archive = deployment.getDescription().getArchive();
        final File explodedDeploymentDirectory = new File(
            tempDirectory + File.separator + testClass.getJavaClass().getCanonicalName() + File.separator + event.getContainer().getName());

        final Archive<?> explodableArchive = getExplodableArchive(testableArchive, archive);

        final File mainArchiveDirectory = new File(explodedDeploymentDirectory, explodableArchive.getName());
        final String mainArchivePath = mainArchiveDirectory.getAbsolutePath();
        File metaDataFile = new File(explodedDeploymentDirectory, explodableArchive.getName() + ".meta");
        boolean alreadyDeployed = metaDataFile.exists();

        if (!alreadyDeployed) {
            processArchiveAndProceedWithDeployment(eventContext, deployment, testableArchive, archive, explodedDeploymentDirectory, mainArchivePath,
                metaDataFile);
        } else {
            SerializableHttpContextData serializableHttpContextData = Serializer.toObject(SerializableHttpContextData.class, metaDataFile);
            explodeIfNeeded(testableArchive, archive, explodedDeploymentDirectory, serializableHttpContextData.isRebelXmlGenerated());
            explodableArchive.as(ExplodedFilterableExporter.class).exportExploded(explodedDeploymentDirectory, new RebelArchiveFilter(archive));
            ProtocolMetaData metaData = new ProtocolMetaData();
            metaData.addContext(serializableHttpContextData.toHTTPContext());
            protocolMetaData.set(metaData);
            deployment.deployed();
            deploymentProducer.set(deployment);
            deploymentDescriptionProducer.set(deployment.getDescription());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
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
     *
     * @return true if rebel.xml was generated; false otherwise
     */
    private boolean addRebelXmlIfNeeded(Archive<?> archive, String rootPath)
    {
        final String path = rootPath + "/" + archive.getName();
        final String archivePath = ArchiveHelper.isWebArchive(archive) ? "WEB-INF/classes/rebel.xml" : "rebel.xml";
        if (archive.get(archivePath) == null) {
            archive.add(new StringAsset(RebelXmlHelper.createRebelXML(archive, path)), archivePath);
            return true;
        }
        return false;
    }

    private boolean addRebelXmlIfNeededToEarModules(EnterpriseArchive enterpriseArchive, String mainArchivePath)
    {
        boolean rebelXmlGenerated = false;
        final Set<Node> rootChildren = enterpriseArchive.get("/").getChildren();
        for (Node node : rootChildren) {
            final Asset asset = node.getAsset();
            if (asset == null) {
                continue;
            }
            if (asset instanceof ArchiveAsset) {
                rebelXmlGenerated |= addRebelXmlIfNeeded(((ArchiveAsset) asset).getArchive(), mainArchivePath);
            }
        }
        return rebelXmlGenerated;
    }

    private boolean addRebelXmlIfNeededToEmbeddedEjbJar(Archive<?> testableArchive, Archive<?> archive, String mainArchivePath)
    {
        for (Node node : testableArchive.getContent().values()) {
            final Asset asset = node.getAsset();
            if (asset instanceof ArchiveAsset) {
                if (archive.equals(((ArchiveAsset) asset).getArchive())) {
                    return addRebelXmlIfNeeded(archive, mainArchivePath + node.getPath().getParent().get());
                }
            }
        }
        return false;
    }

    /**
     * Export archive as exploded directory only if rebel.xml was not generated by this extension and it's the only dynamically generated asset in the archive.
     *
     * @param testableArchive             archive being deployed
     * @param archive                     archive being tested
     * @param explodedDeploymentDirectory directory (parent) to which archive should be exported
     * @param rebelXmlGenerated           flag telling if rebel.xml was generated by this extension
     */
    private void explodeIfNeeded(Archive<?> testableArchive, Archive<?> archive, File explodedDeploymentDirectory, boolean rebelXmlGenerated)
    {
        final RebelArchiveFilter rebelArchiveFilter = new RebelArchiveFilter(archive);
        if (rebelXmlGenerated && (ArchiveHelper.isEarArchive(archive) || !rebelArchiveFilter.isRebelXmlTheOnlyNonFileNonClassAsset())) {
            //noinspection ResultOfMethodCallIgnored
            explodedDeploymentDirectory.mkdirs();
            getExplodableArchive(testableArchive, archive).as(ExplodedFilterableExporter.class).exportExploded(explodedDeploymentDirectory, rebelArchiveFilter);
        }
    }

    private Archive<?> getExplodableArchive(Archive<?> testableArchive, Archive<?> archive)
    {
        return null == testableArchive ? archive : testableArchive;
    }

    /**
     * Check if any archive or it's submodules needs to have rebel.xml generated,
     * export archive with assets that cannot be referenced from disk,
     * proceed with deployment and store metadata.
     *
     * @param eventContext                deployment event context
     * @param deployment                  deployment data
     * @param testableArchive             archive being deployed
     * @param archive                     archive being tested
     * @param explodedDeploymentDirectory directory (parent) to which archive should be exported
     * @param mainArchivePath             path to top exploded archive
     * @param metaDataFile                file to store metadata in
     */
    private void processArchiveAndProceedWithDeployment(EventContext<DeployDeployment> eventContext, Deployment deployment, Archive<?> testableArchive,
                                                        Archive<?> archive, File explodedDeploymentDirectory, final String mainArchivePath, File metaDataFile)
    {
        forcedUndeployment = true;
        try {
            event.fire(new UnDeployDeployment(eventContext.getEvent().getContainer(), deployment));
        } catch (Exception e) {
            final String msg =
                "Cannot undeploy " + deployment.getDescription().getName() + ". Usually it's not a problem. To see more details enable FINE logging.";
            LOGGER.log(Level.INFO, msg);
            LOGGER.log(Level.FINE, "Cannot undeploy " + deployment.getDescription().getName() + ". No such deployment or stale state in target/jrebel-temp", e);
        } finally {
            forcedUndeployment = false;
        }
        boolean rebelXmlGenerated = false;

        if (null != testableArchive && !testableArchive.equals(archive)) {
            rebelXmlGenerated = addRebelXmlIfNeededToEmbeddedEjbJar(testableArchive, archive, mainArchivePath);
        }
        final Archive<?> explodableArchive = getExplodableArchive(testableArchive, archive);
        if (ArchiveHelper.isWebArchive(explodableArchive)) {
            rebelXmlGenerated |= addRebelXmlIfNeeded(explodableArchive, explodedDeploymentDirectory.getAbsolutePath());
        } else if (ArchiveHelper.isEarArchive(explodableArchive)) {
            rebelXmlGenerated = addRebelXmlIfNeededToEarModules((EnterpriseArchive) explodableArchive, mainArchivePath);
        }
        explodeIfNeeded(testableArchive, archive, explodedDeploymentDirectory, rebelXmlGenerated);

        eventContext.proceed();

        HTTPContext httpContext = protocolMetaData.get().getContext(HTTPContext.class);
        if (httpContext != null) {
            //noinspection ResultOfMethodCallIgnored
            metaDataFile.getParentFile().mkdirs();
            Serializer.toStream(new SerializableHttpContextData(httpContext, rebelXmlGenerated), metaDataFile);
        }
    }
}
