package org.jboss.arquillian.extension.jrebel;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Packager {
// -------------------------- STATIC METHODS --------------------------

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
            .addClass(InjectableArtifact.class);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private Packager()
    {
    }
}
