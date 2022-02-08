(defproject pn-mapping "1.0.1"
  :description "Loads triple data into the papyri.info numbers server"
  :repositories {"apache" "https://repository.apache.org/content/repositories/releases/"
                 "apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :jvm-opts ["-Xmx1g" "-Djava.awt.headless=true"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [net.sf.saxon/Saxon-HE "10.5"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.3.04"]
                 [xml-resolver/xml-resolver "1.2"]
                 [org.apache.jena/jena-core "3.10.0"]
                 [org.apache.jena/jena-iri "3.10.0"]
                 [org.apache.jena/jena-arq "3.10.0"]
                 [commons-logging/commons-logging "1.1.1"]
                 [commons-codec/commons-codec "1.7"]]
  :main info.papyri.map
  :aot [info.papyri.map])
