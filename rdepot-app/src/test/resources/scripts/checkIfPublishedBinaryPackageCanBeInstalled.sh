#!/usr/bin/env bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "r-test" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(c(\"lpSolveAPI\", \"ucminf\", \"assertthat\", \"bit64\", \"R6\", \"purrr\", \"tidyselect\"))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(\"arrow\", repos = c(\"rdepot\" = \"http://oa-rdepot-proxy/repo/testrepo2/linux/centos7\"), headers = c(\"User-Agent\" = getOption(\"HTTPUserAgent\")))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'library(\"arrow\")'"
