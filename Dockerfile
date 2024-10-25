FROM ubuntu:focal

# Install Git
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
  apt-get install -y git subversion wget locales build-essential maven openjdk-11-jdk curl

# Set the locale.
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
ENV LEIN_ROOT true

ADD https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein
ADD . /navigator
WORKDIR /navigator
CMD cd pn-dispatcher && mvn test
