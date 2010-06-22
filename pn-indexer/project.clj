(defproject pn-indexer "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [ant/ant-launcher "1.6.2"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]
                 [org.apache.solr/solr-core "1.4.0"]
                 [org.apache.solr/solr-solrj "1.4.0"]
                 [com.hp.hpl.jena/jena "2.6.2"]
                 [net.sf.saxon/saxon9he "9.2.0.2"]]
  :main info.papyri.indexer)