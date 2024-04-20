#!/bin/bash
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app" | cut -d' ' -f1)
docker exec $CONTAINER /bin/bash -c "test `ls -1A /opt/rdepot/generated | wc -l` -eq 0"
