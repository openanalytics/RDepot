#!/bin/bash
CONTAINER_DB=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "postgres" | cut -d' ' -f1)
docker exec $CONTAINER_DB /bin/bash -c "psql -U postgres migrations -c 'TRUNCATE public.databasechangelog'; pg_dump -U postgres -h localhost migrations; echo ENDOFTESTCASE"
