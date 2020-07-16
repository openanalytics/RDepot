#! /bin/bash

clean_up_docker() {
    docker stop $(docker ps -q)
    docker rm $(docker container ls -a -q)
    docker network rm resources_oa-rdepot
}


rdepot_port=8017


make all &&

cd ./app/src/test/resources &&
docker-compose build &&
wget -O itestSource.tar.gz https://cloud.openanalytics.eu/s/js7oDsPnFiWs2jM/download &&
tar -xzf itestSource.tar.gz &&
cd ../../.. &&

docker-compose -f src/test/resources/docker-compose.yaml up -d &&

count=0
status_code=$(curl -I localhost:$rdepot_port 2>/dev/null | grep HTTP/1.1 | cut -d" " -f2)
while [ "$status_code" != "302" ]
do
    echo "$status_code"
    ((count=count+1))
    if [[ $count == 30 ]]; then
        echo "ERROR: RDepot failed to start!"
        clean_up_docker
        cd ./src/test/resources &&
        find . -name "itest*" -type d -exec rm -rf {} + && rm itestSource.tar.gz &&

        exit 1
    fi
    echo "polling / endpoint..."
    sleep 5
    status_code=$(curl -I localhost:$rdepot_port 2>/dev/null | grep HTTP/1.1 | cut -d" " -f2)
done


./gradlew test --tests "RepositoryIntegrationTest" && 
./gradlew test --tests "RepositoryMaintainerIntegrationTest" && 
./gradlew test --tests "UserIntegrationTest" && 
./gradlew test --tests "PackageIntegrationTest" && 
./gradlew test --tests "SubmissionIntegrationTest" && 
./gradlew test --tests "PackageMaintainerIntegrationTest"

clean_up_docker

cd ./src/test/resources &&
find . -name "itest*" -type d -exec rm -rf {} + && 
rm itestSource.tar.gz &&
cd ../../..

