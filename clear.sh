cd docker/app
sudo rm -f rdepot.war
cd ../repo
sudo rm -f rdepot-repo.jar
cd ../..
cd src/integration-test/resources/docker/app
sudo rm -f rdepot.war
cd ../repo
sudo rm -f rdepot-repo.jar
cd ../../
./clearPackages.sh
docker-compose down
cd ../../../
rm -rvf .gradle/*
rm -rvf app/.gradle/*
rm -rvf build/libs/*
rm -rvf app/build/libs/*
rm -rvf repo/build/libs/*
