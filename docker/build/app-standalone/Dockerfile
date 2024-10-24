FROM registry.openanalytics.eu/openanalytics/rdepot-app-it:2.4.1

COPY ./rdepot-app/target/rdepot-app-2.4.1.war /opt/rdepot/rdepot.war
COPY ./docker/build/app-standalone/application.yml /opt/rdepot/application.yml

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
