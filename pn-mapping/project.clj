(defproject pn-mapping "1.0-SNAPSHOT"
  :description "Loads triple data into the papyri.info numbers server"
  :repositories {"papyri.info" "http://dev.papyri.info/maven"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.3.04"]
                 [xml-resolver/xml-resolver "1.2"]
                 [papyri.info.mulgara/querylang "2.1.9"]]
  :dev-dependencies [[lein-marginalia "0.7.0-SNAPSHOT"]]
  :main info.papyri.map)
