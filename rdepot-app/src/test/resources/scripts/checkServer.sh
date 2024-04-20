#!/bin/bash
DIRECTORY_TO_CHECK="/opt/rdepot/testrepo2/src/contrib/Archive/accrued/";

while true; do
    case "$1" in
        -d|--declarative)
            DIRECTORY_TO_CHECK="/opt/rdepot/A/"
            shift;;
        --)
            break;;
        *)
            printf "Unknown option %s\n" "$1"
            exit 1;;
    esac
done

docker exec oa-rdepot-repo /bin/bash -c "cd $DIRECTORY_TO_CHECK"
