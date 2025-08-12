FROM --platform=linux/amd64 tomcat:10-jdk17-temurin-noble

RUN apt-get update && apt-get install -y maven leiningen git

COPY pn-site /srv/data/papyri.info/pn/home

RUN git clone https://github.com/papyri/site-docs.git /srv/data/papyri.info/pn/docs/

# OpenShift runs containers with arbitrary user IDs
# but always with group ID 0 (root group)
# Ensure non root can write to docs
# https://docs.redhat.com/en/documentation/openshift_container_platform/4.18/html/images/creating-images#use-uid_create-images
RUN chgrp -R 0 /srv/data/papyri.info/pn/docs/ && \
    chmod -R g=u /srv/data/papyri.info/pn/docs/

RUN chgrp -R 0 /usr/local/tomcat/webapps/ && \
    chmod -R g=u /usr/local/tomcat/webapps/

COPY /deploy/tomcat/build/maven/settings.xml /.m2/settings.xml

COPY /deploy/tomcat /srv/build/maven

WORKDIR /srv/build/maven

RUN cd build/maven/pn-dispatcher && \
    mvn dependency:copy \
    -Dartifact=info.papyri:pn-dispatcher:1.1.27:war \
    -DoutputDirectory=/usr/local/tomcat/webapps \
    -Dmdep.stripVersion=true

RUN cd build/maven/pn-sync && \
    mvn dependency:copy \
    -Dartifact=info.papyri:pn-sync:1.0.8:war \
    -DoutputDirectory=/usr/local/tomcat/webapps \
    -Dmdep.stripVersion=true

RUN mv /usr/local/tomcat/webapps/pn-dispatcher.war /usr/local/tomcat/webapps/dispatch.war
RUN mv /usr/local/tomcat/webapps/pn-sync.war /usr/local/tomcat/webapps/sync.war