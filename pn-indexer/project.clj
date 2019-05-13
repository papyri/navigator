(defproject pn-indexer "1.0-SNAPSHOT"
  :repositories {"papyri.info" "http://dev.papyri.info/maven/"
                 "apache" "https://repository.apache.org/content/repositories/releases/"
                 "apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :jvm-opts ["-Xms1G" "-Xmx1G" "-Djava.awt.headless=true"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ant/ant-launcher "1.6.2"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]
                 [org.apache.solr/solr-solrj "7.5.0"]
                 [org.apache.jena/jena-core "3.10.0"]
                 [org.apache.jena/jena-iri "3.10.0"]
                 [org.apache.jena/jena-arq "3.10.0"]
                 [net.sf.saxon/Saxon-HE "9.7.0-20"]
                 [com.ibm.icu/icu4j "3.8"]
                 [log4j/log4j "1.2.16"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/slf4j-log4j12 "1.6.1"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.3.04"]
                 [commons-httpclient/commons-httpclient "3.1"]
                 [commons-logging/commons-logging "1.1.1"]
                 [commons-codec/commons-codec "1.5"]]
  :dev-dependencies [[lein-marginalia "0.7.0-SNAPSHOT" :exclusions
                       [org.clojure/clojure]]]
  :main info.papyri.indexer
  :aot [info.papyri.indexer])
