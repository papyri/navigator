FROM tomcat:10-jdk17-temurin-noble AS setup

# Build arguments for versions
ARG PN_DISPATCHER_VERSION=1.1.27
ARG PN_SYNC_VERSION=1.0.8
ARG FUSEKI_URL=fuseki:8090
ARG SOLR_URL=solr:8983

ENV LEIN_HOME=/tmp/.lein

RUN apt-get update && apt-get install -y maven leiningen git

COPY . /srv/data/papyri.info/git/navigator

COPY pn-site /srv/data/papyri.info/pn/home

# OpenShift runs containers with arbitrary user IDs
# but always with group ID 0 (root group)
# Ensure non root can write to docs
# https://docs.redhat.com/en/documentation/openshift_container_platform/4.18/html/images/creating-images#use-uid_create-images


# Create Maven directory
RUN mkdir -p /.m2/repository && \
    chgrp -R 0 /.m2 && \
    chmod -R g=u /.m2

RUN chgrp -R 0 /srv/data/papyri.info/git/navigator/ && \
    chmod -R g=u /srv/data/papyri.info/git/navigator/

RUN mkdir -p /srv/data/papyri.info/pn/idp.html/ && \
    chgrp -R 0 /srv/data/papyri.info/pn/idp.html/ && \
    chmod -R g=u /srv/data/papyri.info/pn/idp.html/

RUN mkdir -p /srv/data/papyri.info/pn/docs/ && \
    chgrp -R 0 /srv/data/papyri.info/pn/docs/ && \
    chmod -R g=u /srv/data/papyri.info/pn/docs/

RUN mkdir -p /srv/data/papyri.info/idp.data/ && \
    chgrp -R 0 /srv/data/papyri.info/idp.data/ && \
    chmod -R g=u /srv/data/papyri.info/idp.data/

RUN chgrp -R 0 /usr/local/tomcat/webapps/ && \
    chmod -R g=u /usr/local/tomcat/webapps/

# Add git safe directories (more secure than writable .gitconfig)
RUN git config --global --add safe.directory /srv/data/papyri.info/pn/docs && \
    git config --global --add safe.directory /srv/data/papyri.info/idp.data && \
    git config --global --add safe.directory /srv/data/papyri.info/pn/idp.html && \
    # For indexing-jobs
    git config --global --add safe.directory /srv/data/papyri.info/git/navigator/epidoc-xslt

# Make Tomcat conf directory writable for OpenShift
RUN chgrp -R 0 /usr/local/tomcat/conf/ && \
    chmod -R g=u /usr/local/tomcat/conf/

# Also make other Tomcat directories writable
RUN chgrp -R 0 /usr/local/tomcat/work/ && \
    chmod -R g=u /usr/local/tomcat/work/ && \
    chgrp -R 0 /usr/local/tomcat/temp/ && \
    chmod -R g=u /usr/local/tomcat/temp/ && \
    chgrp -R 0 /usr/local/tomcat/logs/ && \
    chmod -R g=u /usr/local/tomcat/logs/

FROM setup as maven-build

COPY /deploy/tomcat/build/maven/settings.xml /.m2/settings.xml

COPY /deploy/tomcat /srv/build/maven

WORKDIR /srv/build/maven
# Build from maven registry

RUN cd build/maven/pn-dispatcher && \
    mvn dependency:copy \
    -Dartifact=info.papyri:pn-dispatcher:${PN_DISPATCHER_VERSION}:war \
    -DoutputDirectory=/usr/local/tomcat/webapps \
    -Dmdep.stripVersion=true

RUN cd build/maven/pn-sync && \
    mvn dependency:copy \
    -Dartifact=info.papyri:pn-sync:${PN_SYNC_VERSION}:war \
    -DoutputDirectory=/usr/local/tomcat/webapps \
    -Dmdep.stripVersion=true

RUN mv /usr/local/tomcat/webapps/pn-dispatcher.war /usr/local/tomcat/webapps/dispatch.war
RUN mv /usr/local/tomcat/webapps/pn-sync.war /usr/local/tomcat/webapps/sync.war

FROM setup as fresh-build 

# Build from repo

# Replace localhost domain with okd routes
RUN sed -i "s/localhost:8090/${FUSEKI_URL}/g" /srv/data/papyri.info/git/navigator/pn-dispatcher/src/main/webapp/WEB-INF/web.xml
RUN sed -i "s/localhost:8090/${FUSEKI_URL}/g" /srv/data/papyri.info/git/navigator/pn-indexer/src/info/papyri/indexer.clj
RUN sed -i "s/localhost:8983/${SOLR_URL}/g" /srv/data/papyri.info/git/navigator/pn-dispatcher/src/main/webapp/WEB-INF/web.xml
RUN sed -i "s/localhost:8983/${SOLR_URL}/g" /srv/data/papyri.info/git/navigator/pn-indexer/src/info/papyri/indexer.clj
RUN sed -i "s/localhost:8090/${FUSEKI_URL}/g" /srv/data/papyri.info/git/navigator/pn-mapping/src/info/papyri/map.clj

RUN cd /srv/data/papyri.info/git/navigator/pn-dispatcher && \
    mvn clean package && \
    mv target/dispatch.war /usr/local/tomcat/webapps/dispatch.war

# RUN cd /srv/data/papyri.info/git/navigator/pn-sync && \
#     mvn clean package && \
#     mv target/sync.war /usr/local/tomcat/webapps/sync.war