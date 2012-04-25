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

    private Set<Servlet> servlets;

// --------------------------- CONSTRUCTORS ---------------------------

    public SerializableHttpContextData(HTTPContext context)
    {
        name = context.getName();
        host = context.getHost();
        port = context.getPort();
        for (org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet servlet : context.getServlets()) {
            getServlets().add(new Servlet(servlet.getName(), servlet.getContextRoot()));
        }
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    private Set<Servlet> getServlets()
    {
        if (servlets == null) {
            servlets = new HashSet<Servlet>();
        }
        return servlets;
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
