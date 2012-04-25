package org.jboss.arquillian.extension.jrebel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InjectableArtifact {
// ------------------------------ FIELDS ------------------------------

    private boolean valueSet;

// --------------------- GETTER / SETTER METHODS ---------------------

    public boolean isValueSet()
    {
        return valueSet;
    }

    public void setValueSet(boolean valueSet)
    {
        this.valueSet = valueSet;
    }
}
