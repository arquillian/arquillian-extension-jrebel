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

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InjectableArtifact {
    // ------------------------------ FIELDS ------------------------------

    private boolean valueSet;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public boolean isValueSet() {
        return valueSet;
    }

    public void setValueSet(boolean valueSet) {
        this.valueSet = valueSet;
    }

    /**
     * Run EARDeploymentTestCase once then uncomment this method and you will see that it gets hot deployed.
     */
    //    public String bar() {
    //        return "bar";
    //    }
}
