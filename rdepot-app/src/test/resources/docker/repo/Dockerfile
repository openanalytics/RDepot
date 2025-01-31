FROM registry.openanalytics.eu/proxy/eclipse-temurin:17-jdk-focal

LABEL maintainer="jonas.vanmalder@openanalytics.eu"
ENV RDEPOT_USER rdepot

RUN getent group $RDEPOT_USER || groupadd -r -g 2000 $RDEPOT_USER && id $RDEPOT_USER || useradd -m -d /home/$RDEPOT_USER/ -s /bin/bash -u 2000 -r -g $RDEPOT_USER $RDEPOT_USER

WORKDIR /opt/repo
RUN mkdir /opt/rdepot

RUN chown $RDEPOT_USER:$RDEPOT_USER /opt/repo
RUN chown $RDEPOT_USER:$RDEPOT_USER /opt/rdepot
USER $RDEPOT_USER

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/repo/rdepot-repo.jar --spring.config.location=classpath:/application.properties --spring.config.additional-location=file:/opt/repo/application.yml
