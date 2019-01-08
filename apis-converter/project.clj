(defproject apis-converter "1.0.0-SNAPSHOT"
  :description "Reads APIS input files and converts them to TEI XML"
  :repositories {"papyri.info" "http://dev.papyri.info/maven/"}
  :jvm-opts ["-Xmx256m"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]]
  :main apis_converter.core)
