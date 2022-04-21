(defproject info.papyri/indexer "1.1.19"
  :repositories {"github" "https://maven.pkg.github.com/papyri/navigator"
                 "apache" "https://repository.apache.org/content/repositories/releases/"}
  :jvm-opts ["-Xms1G" "-Xmx1G" "-Djava.awt.headless=true"]
  :deploy-repositories [["releases" {:url "https://maven.pkg.github.com/papyri/navigator"
                                     :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.apache.solr/solr-solrj "8.11.1"]
                 [org.apache.jena/jena-core "4.2.0"]
                 [org.apache.jena/jena-iri "4.2.0"]
                 [org.apache.jena/jena-arq "4.2.0"]
                 [net.sf.saxon/Saxon-HE "10.5"]
                 [xerces/xercesImpl "2.12.2"]
                 [xml-apis/xml-apis "1.4.01"]
                 [org.apache.httpcomponents/httpclient "4.5.13"]
                 [org.apache.httpcomponents/httpcore "4.4.11"]
                 [commons-codec/commons-codec "1.15"]]
  :main info.papyri.indexer
  :aot [info.papyri.indexer])
