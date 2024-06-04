#!/bin/bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "r-test" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(c(\"lpSolveAPI\", \"ucminf\"))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(\"Benchmarking\", repos = c(rdepot = \"http://oa-rdepot-proxy/repo/testrepo2\"))'"
