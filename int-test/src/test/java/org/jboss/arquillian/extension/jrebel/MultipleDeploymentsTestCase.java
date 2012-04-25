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

    private static final String OTHER_WAR_WITH_INJECTABLE_ARTIFACT = "OtherWarWithInjectableArtifact";

    private static final String WAR_WITH_INJECTABLE_ARTIFACT = "WarWithInjectableArtifact";

    @Inject
    InjectableArtifact injectableArtifact;

// -------------------------- STATIC METHODS --------------------------

    @Deployment(name = WAR_WITH_INJECTABLE_ARTIFACT)
    public static WebArchive createFirstArchive()
    {
        return Packager.warWithInjectableArtifact();
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
