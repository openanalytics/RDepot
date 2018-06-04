```
 _____   ____              _
| __  | |    \ ___ ___ ___| |_
|    -| |  |  | -_| . | . |  _|
|__|__| |____/|___|  _|___|_|
                  |_|

```


# R Depot User Guide

Currently, R Depot consists of three core components:

- the RDepot R repository management application (rdepot.war)
- a database component, preferably Postgres
- one or more R repositories (instances of oa-rdepot-repo.jar)

The quickest way to set up a working environment, is by using Docker (preferably on Linux).
The following prerequisites are needed:

- docker
- docker-compose (i.e. by issuing "pip install docker-compose")
- port 80 is not used (localhost, check for nginx or apache service)

Please make sure the following files are available:

- ./docker/app/rdepot.war: downloaded from https://s3-eu-west-1.amazonaws.com/oa-rdepot-build-artifacts/rdepot.war (latest development build)
- ./docker/repo/oa-rdepot-repo.jar: downloaded from https://s3-eu-west-1.amazonaws.com/oa-rdepot-build-artifacts/oa-rdepot-repo.jar (latest development build)

The final step is to launch the Docker containers using docker-compose:

- docker-compose up -d

One can then go to http://localhost to log in.

Regarding users and passwords: all passwords are "password" (without ""), because the application is connected by default to "scientists" group of the ForumSys LDAP server (http://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/).

- user "einstein" is an administrator
- user "tesla" is a repository maintainer (but not linked to a repository, an administrator has to do that manually)
- user "galieleo" is a package maintainer (but not linked to a package, a repository maintainer or administrator has to do that manually)
- user "newton" is a regular user

To complete an end-to-end flow: R package to R Depot to Repository Server to R client, the following steps are needed:

- create a repository with (or edit)
    * a repository's publication URI: http://localhost/repo/repositoryName and
    * a server address: http://oa-rdepot-repo:8080/repositoryName
- submit one or more packages to that repository via the R Depot web interface
- publish the repository using the green button in the repositories view (if needed)
- go to the published package page (repositories view, click repository name, click package name)
- use the install URL shown on the published package page to install the package in R (install.packages("somePackage", repos = c("http://localhost/repo/repositoryName", getOption())))

To (re)start with fresh database:

- remove the containers via "docker-compose down -v"
- start up again using "docker-compose up -d"

Browse the [RDepot Documentation](https://www.rdepot.io/).

#### Copyright (c) Copyright of Open Analytics NV, 2010-2018 - Apache 2.0 License
