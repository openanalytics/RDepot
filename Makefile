app:
	rm -rf ./build/
	rm -rf ./app/build/
	rm -rf ./repo/build/
	./gradlew build -x test -x integrationTest testClasses -x dependencyCheckAggregate #temporary change
	cp -f ./app/build/libs/rdepot-app*application.war ./docker/app/rdepot.war
	cp -f ./app/build/libs/rdepot-app*application.war ./src/integration-test/resources/docker/app/rdepot.war
repo: 
	cp -f ./repo/build/libs/rdepot-repo*application.jar ./docker/repo/rdepot-repo.jar
	cp -f ./repo/build/libs/rdepot-repo*application.jar ./src/integration-test/resources/docker/repo/rdepot-repo.jar
download:
	curl https://s3-eu-west-1.amazonaws.com/oa-rdepot-build-artifacts/rdepot.war --output ./docker/app/rdepot.war
	curl https://s3-eu-west-1.amazonaws.com/oa-rdepot-build-artifacts/rdepot-repo.jar --output ./docker/repo/rdepot-repo.jar
rm-app:
	docker-compose stop app
	docker-compose rm -f app
start-app:
	docker-compose up -d app
all: app repo
.PHONY: all test clean repo app download rm-app start-app
