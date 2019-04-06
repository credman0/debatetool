# unnamed-debate-tool
This project is an attempt to build a cohesive piece of software that helps ease the process of researching and presenting evidence in highschool and college debate.

![Screenshot](https://raw.githubusercontent.com/credman0/unnamed-debate-tool/master/wiki/running_application.png)

## Compilation
You will need to have maven installed, as well as of course a JDK (>=JDK 9). Then you should be able to build an executable jar by running ```mvn -Dmaven.test.skip=true -Djdk.gtk.version=2 prepare-package package```.

## Deploying a database
In the future, I hope to create a comprehensive guide to deploying a functional database. In the mean time, the gist is this:
* Install MongoDB and start a server with auth enabled
* From a local connection, set up an administrator account
* Enter the database IP (and port, if changed from the default) in the software. The setup should happen on its own.
