(defproject apis-converter "1.0.0-SNAPSHOT"
  :description "Reads APIS input files and converts them to TEI XML"
  :repositories {"papyri.info" "http://libdc3-dev-01.oit.duke.edu/maven/"}
  :jvm-opts ["-Xmx256m"]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [net.sf.saxon/saxon-he "9.2.0.2"]]
  :main apis_converter.core)
