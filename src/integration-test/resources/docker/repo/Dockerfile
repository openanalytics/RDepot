FROM registry.openanalytics.eu/proxy/library/openjdk:9

LABEL maintainer="jonas.vanmalder@openanalytics.eu"

#CMD ["java", "$JAVA_","-jar", "/opt/repo/rdepot-repo.jar"]
ENTRYPOINT exec java $JAVA_OPTS -jar /opt/repo/rdepot-repo.jar --spring.config.location=classpath:/application.properties --spring.config.additional-location=file:/opt/repo/application.yml
