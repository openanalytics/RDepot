cd docker/app
sudo rm -rf rdepot.war
cd ../repo
sudo rm -rf rdepot-repo.jar
cd ../..
cd src/integration-test/resources/docker/app
sudo rm -rf rdepot.war
cd ../repo
sudo rm -rf rdepot-repo.jar
cd ../../
./clearPackages.sh
docker-compose -f docker-compose-dev.yaml down
cd ../../../
rm -rvf .gradle/*
rm -rvf app/.gradle/*
rm -rvf build/libs/*
rm -rvf app/build/libs/*
rm -rvf repo/build/libs/*
