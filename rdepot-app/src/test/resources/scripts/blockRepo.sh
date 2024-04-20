#!/bin/bash
#CONTAINER="oa-rdepot-app-without-snapshots";
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app$" | cut -d' ' -f1)

echo "BLOCKING REPO...";

docker exec $CONTAINER /bin/bash -c "echo \"127.0.0.1 oa-rdepot-repo\" >> /etc/hosts"
