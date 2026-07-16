(defproject info.papyri/map "1.0.4"
  :description "Loads triple data into the papyri.info numbers server"
  :license "GPL v3"
  :url "https://github.com/papyri/navigator"
  :repositories [["apache" "https://repository.apache.org/content/repositories/releases/"]
                 ["apache-snapshots" "https://repository.apache.org/content/repositories/snapshots"]
                 ["github" "https://maven.pkg.github.com/papyri/navigator"]
                 ["gitlab-maven" "${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"]]
  :deploy-repositories [["releases" {:url "https://maven.pkg.github.com/papyri/navigator"
                                     :sign-releases false}]]
  :jvm-opts ["-Xmx1g" "-Djava.awt.headless=true"]
  :pom-addition [:distribution-management 
                  [:repository [:id "gitlab-maven"]
                              [:url "${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"]]
                  [:snapshot-repository [:id "gitlab-maven"]
                                       [:url "${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"]]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [net.sf.saxon/Saxon-HE "10.5"]
                 [xerces/xercesImpl "2.12.2"]
                 [xml-apis/xml-apis "1.4.01"]
                 [xml-resolver/xml-resolver "1.2"]
                 [org.apache.jena/jena-core "5.3.0"]
                 [org.apache.jena/jena-iri "5.3.0"]
                 [org.apache.jena/jena-arq "5.3.0"]
                 [org.apache.jena/jena-rdfconnection "5.3.0"]
                 [org.apache.jena/jena-rdfpatch "5.3.0"]
                 [commons-logging/commons-logging "1.2"]
                 [commons-codec/commons-codec "1.11"]]
  :main info.papyri.map
  :aot [info.papyri.map])

