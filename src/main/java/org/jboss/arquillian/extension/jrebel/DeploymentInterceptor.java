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

import java.io.File;

import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.formatter.Formatters;


/**
 * DeploymentInterceptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentInterceptor
{
   private final File tempDirectory;
   
   @Inject @DeploymentScoped
   private InstanceProducer<ProtocolMetaData> protocolMetaData;
   
   public DeploymentInterceptor()
   {
      tempDirectory = ShrinkWrapUtil.createTempDirectory();
   }
   
   public void onDeploy(@Observes(precedence = -1) EventContext<DeployDeployment> eventContext)
   {
      Archive<?> deployment = eventContext.getEvent().getDeployment().getDescription().getTestableArchive();
      File exportPath = new File(tempDirectory, deployment.getName());
      boolean alreadyDeployed = exportPath.exists();
      exportPath.mkdirs();
      deployment.as(ExplodedExporter.class).exportExploded(tempDirectory);
      
      File metaDataFile = new File(tempDirectory, deployment.getName() + ".meta");
      
      if(!alreadyDeployed)
      {
         String rebelXml = createRebelXML(exportPath);
         deployment.add(new StringAsset(rebelXml), "WEB-INF/classes/rebel.xml");
         
         System.out.println(deployment.toString(Formatters.VERBOSE));
         System.out.println();
         System.out.println(rebelXml);
         
         
         eventContext.proceed();

         ProtocolMetaData metaData = protocolMetaData.get();
         Serializer.toStream(metaData, metaDataFile);
      }
      else
      {
         ProtocolMetaData metaData = Serializer.toObject(ProtocolMetaData.class, metaDataFile);
         protocolMetaData.set(metaData);
//         eventContext.proceed();
      }
   }
   
   public void onUnDeploy(@Observes EventContext<UnDeployDeployment> eventContext)
   {
      //eventContext.proceed();
   }

   private String createRebelXML(File output) 
   {
      return  
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
      		"<application\n" + 
      		"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
      		"  xmlns=\"http://www.zeroturnaround.com\"\n" + 
      		"  xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">\n" + 
      		"  <war dir=\"" + output.getAbsolutePath() + "\" />\n" + 
      		/*
      		"  <classpath>\n" + 
      		"    <dir name=\"" + output.getAbsolutePath() + "/WEB-INF/classes\"/>\n" + 
      		"  </classpath>\n" + 
      		"  <web>\n" + 
      		"    <link target=\"/\">\n" + 
      		"       <dir name=\"" + output.getAbsolutePath() + "/\"/>\n" +
      		"    </link>\n" + 
            "    <link target=\"/WEB-INF\">\n" + 
            "       <dir name=\"" + output.getAbsolutePath() + "/WEB-INF/\"/>\n" +
            "    </link>\n" + 
      		"  </web>\n" +
      		*/
      		"</application>";
   }
}
