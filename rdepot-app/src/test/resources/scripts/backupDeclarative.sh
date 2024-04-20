#!/bin/bash
CONTAINER=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "app" | cut -d' ' -f1)
CONTAINER_DB=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "postgres" | cut -d' ' -f1)
CONTAINER_REPO=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "repo" | cut -d' ' -f1)

echo "CREATING DB BACKUP...";

docker exec $CONTAINER_DB su -c "pg_dump -U postgres declarative > /opt/declarative_ready.sql"
