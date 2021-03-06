(defproject pn-indexer "1.0-SNAPSHOT"
  :repositories {"papyri.info" "http://dev.papyri.info/maven/"
                 "apache" "https://repository.apache.org/content/repositories/releases/"
                 "apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :jvm-opts ["-Xms1G" "-Xmx1G" "-Djava.awt.headless=true"]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ant/ant-launcher "1.6.2"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]
                 [org.apache.solr/solr-solrj "7.7.0"]
                 [org.apache.jena/jena-core "3.11.0"]
                 [org.apache.jena/jena-iri "3.11.0"]
                 [org.apache.jena/jena-arq "3.11.0"]
                 [net.sf.saxon/Saxon-HE "9.7.0-20"]
                 [com.ibm.icu/icu4j "3.8"]
                 [log4j/log4j "1.2.16"]
                 [org.slf4j/slf4j-api "1.7.26"]
                 [org.slf4j/slf4j-log4j12 "1.7.26"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.3.04"]
                 [org.apache.httpcomponents/httpclient "4.5.8"]
                 [org.apache.httpcomponents/httpcore "4.4.11"]
                 [commons-logging/commons-logging "1.2"]
                 [commons-codec/commons-codec "1.11"]]
  :main info.papyri.indexer
  :aot [info.papyri.indexer])
