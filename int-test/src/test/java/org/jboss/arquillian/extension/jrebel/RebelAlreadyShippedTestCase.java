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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
public class RebelAlreadyShippedTestCase {
// ------------------------------ FIELDS ------------------------------

    private static final String REBEL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application" +
        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
        "  xmlns=\"http://www.zeroturnaround.com\"" +
        "  xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
        "    <classpath><dir name=\"/tmp/target/classes\"/></classpath>" +
        "</application>";

    private static final String REBEL_XML_PATH = "WEB-INF/classes/rebel.xml";

    private static final Logger log = Logger.getLogger(RebelAlreadyShippedTestCase.class.getName());

    @Inject
    private InjectableArtifact injectableArtifact;

// -------------------------- STATIC METHODS --------------------------

    private static String asUTF8String(InputStream in)
    {
        StringBuilder buffer = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    buffer.append("\n");
                }
                firstLine = false;
                buffer.append(line);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error in obtaining string from " + in, ioe);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("Could not close stream due to: " + ignore.getMessage() + "; ignoring");
                }
            }
        }

        return buffer.toString();
    }

    @Deployment
    public static WebArchive createArchive()
    {
        return Packager.warWithInjectableArtifact(RebelAlreadyShippedTestCase.class).add(new StringAsset(REBEL_XML), REBEL_XML_PATH);
    }

// -------------------------- OTHER METHODS --------------------------

    @Test
    public void checkRebel() throws Exception
    {
        final InputStream resourceAsStream = injectableArtifact.getClass().getClassLoader().getResourceAsStream("rebel.xml");
        Assert.assertNotNull(resourceAsStream);
        Assert.assertEquals(REBEL_XML, asUTF8String(resourceAsStream));
    }
}
