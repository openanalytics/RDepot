#!/usr/bin/env bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "r-test" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "apt-get install -y libssl-dev"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(c(\"carData\", \"abind\", \"Formula\"))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'install.packages(\"car\", repos = c(rdepot_binary = \"http://oa-rdepot-proxy/repo/my-test-repo-123/linux/centos7\"), headers = c(\"User-Agent\" = getOption(\"HTTPUserAgent\")))'"
docker exec $CONTAINER_TEST /bin/bash -c "R -e 'library(\"car\")'"
