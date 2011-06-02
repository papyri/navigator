(defproject pn-mapping "1.0.0"
  :description "Loads triple data into the papyri.info numbers server"
  :repositories {"papyri.info" "http://dev.papyri.info/maven"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]
                 [xerces/xercesImpl "2.9.1"]
                 [xml-apis/xml-apis "1.0.b2"]
                 [xml-resolver/xml-resolver "1.2"]
                 [papyri.info.mulgara/querylang "2.1.9"]]
  :main info.papyri.map)
