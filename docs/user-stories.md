# RDepot user stories

When mentioning viewing resources, this is only for the non-(soft-)deleted resources, unless mentioned otherwise.

## Roles:
 1. [Guest](#guest-role) - unauthenticated user
 2. [User](#user-role) - the least privileged role in RDepot for authenticated users
 3. [Package maintainer](#package-maintainer-role)
 4. [Repository maintainer](#repository-maintainer-role)
 5. [Admin](#admin-role) - the most privileged role in RDepot

## Guest role

As a guest, I can:
- log in to the system [1.1]
- access the repo server [1.2]
- see published package details [1.3]
- install published packages in R [1.4]
- download published package sources [1.5]
- download published package vignettes [1.6]
- download published package reference manuals [1.7]

## User role

As a user, I can:
- access the repo server [2.1]
- log out from RDepot [2.2]
- see details about my account [2.3]
- submit packages [2.4]
- browse packages [2.5]
- browse all submissions [2.6]
- cancel my submissions if and only if they haven't been approved [2.7]
- browse repositories [2.8]
- see packages in a given repository [2.9]
- see package details [2.10]
- install published packages in R [2.11]
- download published package sources [2.12]
- download published package vignettes [2.13]
- download published package reference manuals [2.14]
- browse package archives [2.15]
- create access tokens [2.16]
- manage my access tokens [2.17]

## Package maintainer role

As a package maintainer, I can:
- do the same things as a user, and [3.1]
- submit packages with auto-approval of maintained packages [3.2]
- activate or deactivate maintained packages [3.3]
- delete maintained packages [3.4]
- approve or reject submissions of maintained packages [3.5]

## Repository maintainer role

As a repository maintainer, I can:
- do the same things as a package manager, and [4.1]
- submit packages with auto-approval in maintained repositories [4.2]
- publish/unpublish maintained repositories [4.3]
- update name, publication URI and server address of maintained repositories [4.4]
- approve/reject submissions in maintained repositories [4.5]
- synchronize maintained repositories which are mirrored [4.6]
- see synchronization status of maintained repositories which are mirrored [4.7]
- see the newsfeed [4.8]
- update the newsfeed [4.9]
- activate or deactivate packages in maintained repositories [4.10]
- delete packages in maintained repositories [4.11]
- browse package maintainers in maintained repositories [4.12]
- create package maintainer in maintained repositories [4.13]
- delete package maintainer in maintained repositories [4.14]
- update package maintainer in maintained repositories [4.15]
- see deleted package maintainers in maintained repositories [4.16]

##  Admin role

As an admin, I can:
- do the same things as a repository maintainer (in any package or repository), and [5.1]
- submit packages to any repository with auto-approval [5.2]
- accept/reject any submission [5.4]
- see deleted submissions [5.5]
- permanently delete submissions (hard delete) [5.6]
- create repositories [5.7]
- see deleted repositories [5.8]
- permanently delete any repository (hard delete) [5.9]
- see deleted packages [5.10]
- permanently delete any package (hard delete) [5.11]
- browse all package maintainers [5.12]
- create package maintainers in any repository [5.14]
- modify any package maintainer [5.15]
- permanently delete any package maintainer (hard delete) [5.16]
- browse all repository maintainers [5.17]
- see deleted repository maintainers [5.18]
- create repository maintainers [5.19]
- delete any repository maintainers [5.20]
- update any repository maintainers [5.21]
- permanently delete any repository maintainer (hard delete) [5.22]
- browse all users [5.23]
- browse all roles [5.24]
- change the role of any user [5.25]
- activate or deactivate users [5.26]
- see users' access tokens [5.27]
- modify users' access tokens [5.28]
