#!/bin/bash

echo "" > ./docker/db/rdepot_insert.sql

while getopts ":R:" o; do
	case "${o}" in
		R)
			echo "INSERT INTO public.repository (version, id, publication_uri, name, server_address, published, deleted) VALUES (0, 1, 'http://localhost/repo/${OPTARG}', '${OPTARG}', 'http://oa-rdepot-repo:8080/${OPTARG}', 'f', 'f');" > ./docker/db/rdepot_insert.sql
			;;
	esac
done

docker-compose down && make all && docker-compose build && docker-compose up
