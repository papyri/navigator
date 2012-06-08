(defproject pn-indexer "1.0-SNAPSHOT"
  :repositories {"papyri.info" "http://dev.papyri.info/maven"
                 "apache" "https://repository.apache.org/content/repositories/releases/"
                 "apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :jvm-opts ["-Xmx1g"]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [ant/ant-launcher "1.6.2"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]
                 [org.apache.solr/solr-core "3.5.0"]
                 [org.apache.solr/solr-solrj "3.5.0"]
                 [org.apache.solr/solr-commons-csv "3.5.0"]
                 [org.apache.jena/jena-core "2.7.1-SNAPSHOT"]
                 [org.apache.jena/jena-iri "0.9.1-SNAPSHOT"]
                 [org.apache.jena/jena-arq "2.9.1-SNAPSHOT"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]
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
  :main info.papyri.indexer)
