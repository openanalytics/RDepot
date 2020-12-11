#!/bin/bash

psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER -d $POSTGRES_DB -f /opt/rdepot.sql
