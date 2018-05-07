FROM openjdk:slim

LABEL MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

RUN echo 'Acquire::ForceIPv4 "true";' | tee /etc/apt/apt.conf.d/99force-ipv4
RUN apt-get update -y && apt-get install -y r-base texlive texinfo texlive-fonts-extra

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war