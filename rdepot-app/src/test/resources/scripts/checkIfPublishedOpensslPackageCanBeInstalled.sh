#!/bin/bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "r-test" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "apt-get install -y libssl-dev"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(c(\"askpass\", \"ucminf\"))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(\"openssl\", repos = c(rdepot = \"http://oa-rdepot-proxy/repo/my-test-repo-123\"))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'library(\"openssl\")'"
