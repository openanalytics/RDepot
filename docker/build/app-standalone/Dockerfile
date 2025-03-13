FROM registry.openanalytics.eu/openanalytics/rdepot-app-it:2.5.2

COPY ./rdepot-app/target/rdepot-app-2.5.2.war /opt/rdepot/rdepot.war
COPY ./docker/build/app-standalone/application.yml /opt/rdepot/application.yml

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
