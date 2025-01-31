## This is a demo RDepot deployment.

### How to start RDepot

Firstly, environment variable `RDEPOT_PORT` can be defined, for instance by executing the following command in the terminal:

`export RDEPOT_PORT=8080`

This port will be used for serving the RDepot UI (if not set, it defaults to 80).
Make sure that the defined port above (or port 80, if not set) is available on the machine.

When that is done, simply run:

`docker compose up`

to start in the foreground or

`docker compose up -d`

to run in the background.

### How to stop RDepot

To turn off RDepot, execute:

`docker compose down`

or

`docker compose down -v`

to remove all data.

### How to interact with RDepot

After RDepot is ready, one can then browse to `http://localhost:${RDEPOT_PORT}` to log in (where `${RDEPOT_PORT}` is the value of the environment variable defined above, or 80 if not set).

There is an admin user with username `einstein` and password `testpassword`.

See [the RDepot manager application.yaml](./docker/compose/backend/application.yaml) for the included configuration which specifies the user credentials. One can override these parameters using [environment variables](https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-relaxed-binding-from-environment-variables).

For more information on configuration, please check [the official documentation](https://rdepot.io/latest/documentation/administration/configuration/).

To complete an end-to-end flow, which consists of:

1. upload R or Python package via the RDepot UI
2. publish the package to the RDepot Repository server
3. install the R or Python package

the following steps are needed:

1. go to the repositories page (see `http://localhost:${RDEPOT_PORT}/repositories`)
2. create a repository with:
    * a name: in this demo deployment, Python repository names should use a `py` prefix, for example: `pyDemoRepo` (R repositories have no such restrictions)
    * a publication URI: `http://localhost:${RDEPOT_PORT}/repo/repositoryName` (again, replace `${RDEPOT_PORT}` with the above defined port, or 80 if not set)
    * a server address: http://oa-rdepot-repo:8080/repositoryName
    * a technology: R or Python
3. make sure the repository is shown in the UI by toggling the `Published` filter toggle in the UI, above the `Repositories` table
4. publish the repository using the `Published` checkbox on the repositories list page
5. submit one or more packages to that repository via the RDepot UI (see `http://localhost:${RDEPOT_PORT}/upload-packages`)
6. go to the package details page: from the packages view at `http://localhost:${RDEPOT_PORT}/packages`, click on a package name and then on the details button
7. use the install command shown on the package details page to install the package in R or Python:

R:
```
install.packages("somePackage", repos = c("http://localhost:${RDEPOT_PORT}/repo/repositoryName", getOption("repos")))
```

or

Python:
```
pip install --index-url http://localhost:${RDEPOT_PORT}/repo/repositoryName somePackage
```

More information can be found on the RDepot website:
* [User guide](https://rdepot.io/latest/documentation/user-guide/)
* [Deployment configuration](https://rdepot.io/latest/documentation/administration/deployment/)
