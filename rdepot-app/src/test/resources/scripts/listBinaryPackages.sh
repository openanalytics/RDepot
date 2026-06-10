#!/bin/bash
CONTAINER_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "repo" | cut -d' ' -f1)
docker exec $CONTAINER_TEST /bin/sh -c "find /opt/rdepot/my-test-repo-123/bin -type f -exec md5sum '{}' \; | sort | grep -v archive.rds"
