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

First time you run test it will deploy the package, export the deployment to target/jrebel-temp directory and attach auto generated rebel.xml file that instructs JRebel to override deployed package with the one exported to target/jrebel-temp. Next time you run the test Arquillian will check if package exists in the temp directory and if so it will run tests without deploying the package.

If you want to force Arquillian to deploy the package again to the container you have to delete the temp directory.


[jrebel]: http://zeroturnaround.com/jrebel/
