FROM openanalytics/rdepot-app-it:1.7.7

COPY ./app/build/libs/rdepot-app-*application.war /opt/rdepot/rdepot.war
COPY ./docker/app/application.yml /opt/rdepot/application.yml

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
