FROM openanalytics/rdepot-app-it:1.7.4

COPY rdepot-app-*application.war /opt/rdepot/rdepot.war

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
