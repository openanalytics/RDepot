FROM registry.openanalytics.eu/proxy/eclipse-temurin:17-jdk-focal

LABEL MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

ENV DEBIAN_FRONTEND=noninteractive
ENV RDEPOT_USER rdepot

# RUN echo 'Acquire::ForceIPv4 "true";' | tee /etc/apt/apt.conf.d/99force-ipv4
RUN apt-get update -y && apt-get install -y r-base texlive texinfo texlive-fonts-extra mc net-tools

RUN getent group $RDEPOT_USER || groupadd -r -g 2000 $RDEPOT_USER && id $RDEPOT_USER || useradd -m -d /home/$RDEPOT_USER/ -s /bin/bash -u 2000 -r -g $RDEPOT_USER $RDEPOT_USER

WORKDIR /opt/rdepot
RUN mkdir /opt/rdepot/new /opt/rdepot/repositories /opt/rdepot/generated
RUN chown -R $RDEPOT_USER:$RDEPOT_USER /opt/rdepot
USER $RDEPOT_USER

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
