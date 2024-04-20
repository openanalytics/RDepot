#!/bin/bash
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app" | cut -d' ' -f1)
CONTAINER_DB=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "postgres" | cut -d' ' -f1)
CONTAINER_REPO=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "repo" | cut -d' ' -f1)

while [ $# -gt 0 ]; do
    case "$1" in
        -s|--snapshots)
            CONTAINER="oa-rdepot-app"
            shift;;
        --)
            break;;
        *)
            printf "Unknown option %s\n" "$1"
            exit 1;;
    esac
done

docker exec $CONTAINER_DB su - postgres -c "dropdb declarative; createdb declarative; psql declarative < /opt/declarative_ready.sql";
docker exec $CONTAINER_DB su - root -c "rm -f /opt/declarative_ready.sql";
docker exec $CONTAINER /bin/bash -c "rm -rf /opt/rdepot/repositories; rm -rf /opt/rdepot/new; rm -rf /opt/rdepot/generated; mkdir -p /opt/rdepot/generated";

if [ $? -eq 0 ]; then
	echo "FILES RESTORED";
fi
