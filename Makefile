app:
	./gradlew build
	cp -f ./build/libs/rdepot.war ./docker/app/rdepot.war
repo:
	cd ./repo && ./gradlew build && cp -f ./build/libs/oa-rdepot-repo*.jar ./../docker/repo/oa-rdepot-repo.jar
docker-db:
	-docker stop oa-rdepot-db
	-docker rm -v oa-rdepot-db
	-docker rmi oa/rdepot/db
	docker build ./docker/db/ -t oa/rdepot/db
docker-app:
	-docker stop oa-rdepot-app
	-docker rm -v oa-rdepot-app
	-docker rmi oa/rdepot/app
	docker build ./docker/app/ -t oa/rdepot/app
docker-repo:
	-docker stop oa-rdepot-repo
	-docker rm -v oa-rdepot-repo
	-docker rmi oa/rdepot/repo
	docker build ./docker/repo/ -t oa/rdepot/repo
docker: app repo docker-db docker-app docker-repo
.PHONY: all test clean repo app docker-db docker-app docker-repo
