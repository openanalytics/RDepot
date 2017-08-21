FROM nginx:alpine

MAINTAINER Jonas Van Malder "jonas.vanmalder@openanalytics.eu"

RUN apk add --no-cache openjdk8

RUN rm /etc/nginx/conf.d/default.conf
RUN rm /etc/nginx/nginx.conf

COPY nginx.conf /etc/nginx/
COPY repo /etc/nginx/sites-enabled/

RUN mkdir -p /opt/repo
RUN mkdir -p /opt/rdepot

COPY oa-rdepot-repo.jar /opt/repo/oa-rdepot-repo.jar

EXPOSE 8080 80

CMD nginx ; java -jar /opt/repo/oa-rdepot-repo.jar
