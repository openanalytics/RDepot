app:
	rm -rf ./build
	./gradlew build
	cp -f ./build/libs/rdepot*.war ./docker/app/rdepot.war
repo:
	rm -rf ./repo/build
	cd ./repo && ./gradlew build && cp -f ./build/libs/oa-rdepot-repo*.jar ./../docker/repo/oa-rdepot-repo.jar
all: app repo
.PHONY: all test clean repo app
