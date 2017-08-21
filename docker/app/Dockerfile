FROM tomcat:latest

MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

RUN echo 'Acquire::ForceIPv4 "true";' | tee /etc/apt/apt.conf.d/99force-ipv4
RUN apt-get update -y && apt-get install -y r-base texlive texinfo texlive-fonts-extra

COPY rdepot.war /tmp/rdepot.war
RUN mkdir -p /usr/local/tomcat/webapps/rdepot
RUN unzip -o /tmp/rdepot.war -d /usr/local/tomcat/webapps/rdepot/
RUN rm -f /tmp/rdepot.war

RUN sed -i 's#db.url=jdbc:postgresql://localhost:5432/rdepot#db.url=jdbc:postgresql://oa-rdepot-db:5432/postgres#' /usr/local/tomcat/webapps/rdepot/WEB-INF/classes/application.properties
RUN sed -i 's#db.username=rdepot#db.username=postgres#' /usr/local/tomcat/webapps/rdepot/WEB-INF/classes/application.properties

RUN mkdir -p /opt/rdepot/generated
