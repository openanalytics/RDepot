#!/bin/bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "python" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/bash -c "pip install httplib2"
docker exec $CONTAINER_TEST /bin/bash -c "pip install --trusted-host oa-rdepot-proxy --index-url http://oa-rdepot-proxy/repo/testrepo8 coconutpy"
