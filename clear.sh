cd src/integration-test/resources/docker/app
sudo rm -rf rdepot.war
cd ../repo
sudo rm -rf rdepot-repo.jar
cd ../../
./clearPackages.sh
docker-compose down
cd ../../../
rm -rvf .gradle/*
rm -rvf app/.gradle/*

