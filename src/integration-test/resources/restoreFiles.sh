#! /bin/bash

docker exec oa-rdepot-app /bin/bash -c 'mkdir -p /opt/rdepot/repositories; mkdir -p /opt/rdepot/new; mkdir -p /opt/rdepot/generated; cp -fr /opt/testSourceFiles/info/* /opt/rdepot/repositories; cp -fr /opt/testGenerated/repository/* /opt/rdepot/generated; cp -fr /opt/newFiles/new/* /opt/rdepot/new'
