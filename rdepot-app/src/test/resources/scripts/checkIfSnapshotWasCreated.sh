#!/bin/bash

CONTAINER="oa-rdepot-app";

while true; do
    case "$1" in
        -d|--declarative)
            CONTAINER="oa-rdepot-app-declarative"
            shift;;
        --)
            break;;
        *)
            printf "Unknown option %s\n" "$1"
            exit 1;;
    esac
done

docker exec $CONTAINER /bin/bash -c "cd /opt/rdepot/generated/5/"
