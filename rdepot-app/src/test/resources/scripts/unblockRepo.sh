#!/bin/bash
#CONTAINER="oa-rdepot-app-without-snapshots";
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app$" | cut -d' ' -f1)

echo "UNBLOCKING REPO...";

docker exec $CONTAINER /bin/bash -c "head -n -1 /etc/hosts > /etc/hosts.tmp && cat /etc/hosts.tmp > /etc/hosts && rm /etc/hosts.tmp"

