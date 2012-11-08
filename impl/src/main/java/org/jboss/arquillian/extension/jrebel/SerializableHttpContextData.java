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

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SerializableHttpContextData implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private String host;

    private String name;

    private int port;

    private boolean rebelXmlGenerated;

    private Set<Servlet> servlets;

// --------------------------- CONSTRUCTORS ---------------------------

    public SerializableHttpContextData(HTTPContext context, boolean rebelXmlGenerated)
    {
        name = context.getName();
        host = context.getHost();
        port = context.getPort();
        for (org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet servlet : context.getServlets()) {
            getServlets().add(new Servlet(servlet.getName(), servlet.getContextRoot()));
        }
        this.rebelXmlGenerated = rebelXmlGenerated;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    private Set<Servlet> getServlets()
    {
        if (servlets == null) {
            servlets = new HashSet<Servlet>();
        }
        return servlets;
    }

    public boolean isRebelXmlGenerated()
    {
        return rebelXmlGenerated;
    }

// -------------------------- OTHER METHODS --------------------------

    public HTTPContext toHTTPContext()
    {
        final HTTPContext httpContext = new HTTPContext(name, host, port);
        for (Servlet servlet : getServlets()) {
            httpContext.add(new org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet(servlet.name, servlet.contextRoot));
        }
        return httpContext;
    }

// -------------------------- INNER CLASSES --------------------------

    private class Servlet implements Serializable {
// ------------------------------ FIELDS ------------------------------

        private String contextRoot;

        private String name;

// --------------------------- CONSTRUCTORS ---------------------------

        public Servlet(String name, String contextRoot)
        {
            this.name = name;
            this.contextRoot = contextRoot;
        }
    }
}
