#!/bin/bash

for file in /opt/sql_files/*.sql;
do
    echo "Processing $file"
    result=$(basename $file .sql)
    echo "$result"
    psql -v ON_ERROR_STOP=1 -U postgres <<-EOSQL
	    CREATE DATABASE $result;
	    GRANT ALL PRIVILEGES ON DATABASE $result TO postgres;
	EOSQL
	if [ "$result" == "declarative" ]; then
        psql -U postgres -f $file
	fi
done
