#!/usr/bin/env bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "python" | cut -d' ' -f1)
PROXY_CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "nginx:alpine" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "pip install --trusted-host oa-rdepot-proxy --index-url http://oa-rdepot-proxy/repo/testrepo8 coconutpy"
CHECK_RDEPOT=$(docker exec $CONTAINER_TEST /bin/bash -c "pip show coconutpy | grep RDEPOT-VERSION")
if [ -z "$CHECK_RDEPOT" ]; then
    exit 123;
fi
docker exec $CONTAINER_TEST /bin/bash -c "python -c 'import coconut'"
