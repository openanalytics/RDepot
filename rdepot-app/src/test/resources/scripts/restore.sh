#!/bin/bash
#CONTAINER="oa-rdepot-app-without-snapshots";
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app$" | cut -d' ' -f1)
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

echo "RESTORING $CONTAINER...";

docker exec $CONTAINER /bin/sh -c "rm -rvf /opt/rdepot/repositories; rm -rf /opt/rdepot/generated; mkdir -vp /opt/rdepot/repositories; mkdir -vp /opt/rdepot/new; mkdir -vp /opt/rdepot/generated; mkdir -vp /opt/rdepot/trash; cp -fvr /opt/testSourceFiles/info/* /opt/rdepot/repositories; cp -fvr /opt/testGenerated/repository/* /opt/rdepot/generated; cp -fvr /opt/newFiles/new/* /opt/rdepot/new; cp -fvr /opt/trashFiles/trash/* /opt/rdepot/trash";

echo "RESTORING $CONTAINER_DB";

docker exec $CONTAINER_DB su - postgres -c "psql rdepot -c 'TRUNCATE public.access_token, public.changed_variable, public.newsfeed_event, public.submission, public.repository_maintainer, public.package_maintainer, public.rpackage, public.package, public.rrepository, public.pythonpackage, public.pythonrepository, public.repository, public.api_token, public.user, public.user_settings'; psql rdepot < /opt/sql_files/rdepot.sql"
docker exec $CONTAINER_REPO /bin/sh -c "rm -r /opt/rdepot/*; cp -rf /opt/testServer/* /opt/rdepot";

if [ $? -eq 0 ]; then
	echo "FILES RESTORED";
fi
