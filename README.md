<pre>
 _____   ____              _
| __  | |    \ ___ ___ ___| |_
|    -| |  |  | -_| . | . |  _|
|__|__| |____/|___|  _|___|_|
                  |_|

</pre>


# R Depot User Guide

Currently, R Depot consists of three core components:

- the RDepot R repository management application (rdepot-app-X.X.X.war)
- a database component, preferably Postgres
- one or more R repositories (instances of rdepot-repo-X.X.X.jar)

The quickest way to set up a working environment, is by using Docker (preferably on Linux).
The following prerequisites are needed:

- docker
- docker-compose (i.e. by issuing "pip install docker-compose")
- port 80 is not used (localhost, check for nginx or apache service)

Please make sure the following files are available:

- ./docker/app/webapps/rdepot.war: downloaded and renamed from https://www.rdepot.io/downloads/rdepot-app-0.9.0.war
- ./docker/repo/oa-rdepot-repo.jar: downloaded and renamed from https://www.rdepot.io/downloads/rdepot-repo-1.0.0.jar

The final step is to launch the Docker containers using docker-compose:

- docker-compose up -d

One can then go to http://localhost to log in.

Regarding users and passwords: all passwords are "password" (without ""), because the application is connected by default to "scientists" group of the ForumSys LDAP server (http://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/).

- user "einstein" is an administrator
- user "tesla" is a repository maintainer (but not linked to a repository, an administrator has to do that manually)
- user "galieleo" is a package maintainer (but not linked to a package, a repository maintainer or administrator has to do that manually)
- user "newton" is a regular user

To complete a complete flow: R package to R Depot to Repository Server to R client, the following steps are needed:

- create a repository with (or edit)
    * a repository's publication URI: http://localhost/repo/repositoryName and
    * a server address: http://localhost/repo-api/repositoryName
- submit one or more packages to that repository via the R Depot web interface
- publish the repository using the green button in the repositories view (if needed)
- go to the published package page (repositories view, click publication URI, click package name)
- use the install URL shown on the published package page to install the package in R (install.packages("somePackage", repos = c("http://localhost/repo/repositoryName", getOption()), type = "source"))

To (re)start with fresh database:

- remove the containers via "docker-compose down -v"
- start up again using "docker-compose up -d"

#### Copyright (c) Copyright of Open Analytics NV, 2010-2018 - GNU AFFERO GENERAL PUBLIC LICENSE
