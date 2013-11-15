JRebel - The Arquillian Experience
==================================

This extension is for speeding up test development cycle. It deploys the package only once and then leverages [JRebel][jrebel] to hot deploy changed files.

Usage
-----

Just add impl module to classpath and run test either from IDE or maven.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-jrebel-impl</artifactId>
        <version>1.0.0.Alpha1-SNAPSHOT</version>
    </dependency>

**Make sure you use servlet protocol!** To do that add following to arquillian.xml:

    <defaultProtocol type="Servlet 3.0"/>

First time you run test it will deploy the package, export the deployment to `target/jrebel-temp` directory
and attach auto generated rebel.xml file that instructs JRebel to override deployed package with the one exported to `target/jrebel-temp`.
Next time you run the test Arquillian will check if package exists in the temp directory and if so it will run tests without deploying the package.

If you want to force Arquillian to deploy the package again to the container you have to delete the `target/jrebel-temp` directory.


I'm getting exceptions
---

If you happen to get exception like the one below you probably do not have deployment on the server and do have metadata in `target/jrebel-temp`.
Just remove that directory and re-run your tests.

```
java.lang.IllegalStateException: Error launching test org.jboss.arquillian.extension.jrebel.RebelAlreadyShippedTestCase public void org.jboss.arquillian.extension.jrebel.RebelAlreadyShippedTestCase.checkRebel() throws java.lang.Exception
	at org.jboss.arquillian.protocol.servlet.ServletMethodExecutor.invoke(ServletMethodExecutor.java:126)
	at org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter.execute(RemoteTestExecuter.java:120)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:606)
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90)
	at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
	at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:135)
	at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:115)
	at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67)
	at org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter.execute(ClientTestExecuter.java:57)
	...
Caused by: java.lang.IllegalStateException: Error launching request at http://127.0.0.1:8080/RebelAlreadyShippedTestCase/ArquillianServletRunner?outputMode=serializedObject&className=org.jboss.arquillian.extension.jrebel.RebelAlreadyShippedTestCase&methodName=checkRebel. No result returned
	at org.jboss.arquillian.protocol.servlet.ServletMethodExecutor.executeWithRetry(ServletMethodExecutor.java:162)
	at org.jboss.arquillian.protocol.servlet.ServletMethodExecutor.invoke(ServletMethodExecutor.java:122)
	... 77 more
```

[jrebel]: http://zeroturnaround.com/jrebel/
