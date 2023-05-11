#!/bin/bash
docker exec oa-rdepot-app-without-snapshots /bin/bash -c 'ls /opt/rdepot/generated';

if [ $? -ne 0 ]; then
    exit 1;
fi

COUNT=$(docker exec oa-rdepot-app-without-snapshots /bin/bash -c 'ls -1A /opt/rdepot/generated | wc -l')

if [ $COUNT -ne 0 ]; then
    exit 1;
else
    exit 0;
fi
