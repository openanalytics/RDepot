FROM registry.openanalytics.eu/proxy/eclipse-temurin:17-jdk-focal

LABEL MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

ENV DEBIAN_FRONTEND=noninteractive

RUN echo 'Acquire::ForceIPv4 "true";' | tee /etc/apt/apt.conf.d/99force-ipv4
RUN apt-get update -y && apt-get install -y r-base texlive texinfo texlive-fonts-extra mc net-tools

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war --spring.config.location=classpath:/application.yml --spring.config.additional-location=file:/opt/rdepot/application.yml
