#! /bin/bash

docker exec oa-rdepot-repo /bin/sh -c "rm -r /opt/rdepot/*; cp -rf /opt/testServer/* /opt/rdepot"
