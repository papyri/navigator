(defproject pn-mapping "1.0-SNAPSHOT"
  :description "Loads triple data into the papyri.info numbers server"
  :repositories {"papyri.info" "http://dev.papyri.info/maven"
                 "apache" "https://repository.apache.org/content/repositories/releases/"
                 "apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :jvm-opts ["-Xmx1g"]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.3.04"]
                 [xml-resolver/xml-resolver "1.2"]
                 [org.apache.jena/jena-core "2.7.1"]
                 [org.apache.jena/jena-iri "0.9.1"]
                 [org.apache.jena/jena-arq "2.9.1"]
                 [org.apache.jena/jena-fuseki "0.2.2"]
                 [commons-logging/commons-logging "1.1.1"]
                 [commons-codec/commons-codec "1.7"]]
  :main info.papyri.map)
