FROM openjdk:slim

LABEL MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

ARG http_proxy
ARG https_proxy
ARG no_proxy
ENV HTTPS_PROXY $https_proxy
ENV https_proxy $https_proxy
ENV http_proxy $http_proxy
ENV no_proxy $no_proxy

RUN echo 'Acquire::ForceIPv4 "true";' | tee /etc/apt/apt.conf.d/99force-ipv4
RUN apt-get update -y && apt-get install -y r-base texlive texinfo texlive-fonts-extra

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/rdepot/rdepot.war