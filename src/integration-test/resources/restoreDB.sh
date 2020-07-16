#! /bin/bash

docker exec oa-rdepot-db su - postgres -c "dropdb postgres; createdb postgres; psql < /opt/database.sql"
