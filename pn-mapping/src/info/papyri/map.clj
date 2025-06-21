;; Recursively read a directory full of XML and convert the files therein to RDF, using a provided XSLT.
;; Then load the RDF data into a triplestore

(ns info.papyri.map
  (:gen-class
   :name info.papyri.map
   :methods [#^{:static true} [deleteGraph [] void]
            #^{:static true} [deleteUri [String] void]
            #^{:static true} [loadFile [String] void]
            #^{:static true} [insertInferences [String] void]
            #^{:static true} [mapFiles [java.util.List] void]
            #^{:static true} [mapAll [java.util.List] void]
            ])
  (:require [clojure.string :as str])
  (:import (java.io BufferedReader ByteArrayInputStream ByteArrayOutputStream File FileInputStream FileOutputStream FileReader StringReader StringWriter)
           (java.net URI)
           (java.nio.charset Charset)
           (java.util.concurrent Executors ConcurrentLinkedQueue)
           (javax.xml.transform Templates Transformer)
           (javax.xml.transform.stream StreamSource StreamResult)
           (net.sf.saxon.s9api Destination Processor QName SAXDestination Serializer XdmAtomicValue XsltCompiler XsltExecutable)
           (org.apache.jena.graph Node)
           (org.apache.jena.sparql.exec.http QueryExecutionHTTP)
           (org.apache.jena.rdf.model Model ModelFactory)
           (org.apache.jena.riot RDFLanguages RDFParser)
           (org.apache.jena.riot.system ErrorHandlerFactory)
           (org.apache.jena.rdfconnection RDFConnection RDFConnectionFuseki)
           (org.apache.jena.sparql.modify.request UpdateCreate UpdateLoad)
           (org.apache.jena.sparql.lang UpdateParser)
           (org.apache.jena.update Update UpdateExecutionFactory UpdateFactory UpdateRequest)
           (org.apache.jena.update Update UpdateExecutionFactory UpdateFactory UpdateRequest)
           (org.apache.commons.codec.digest DigestUtils)))

(def xsl (ref nil))
(def pxslt (ref nil))
(def processor (Processor. false))
(def buffer (ref nil))
(def flushing (ref false))
(def output (ref nil))
(def server "http://localhost:8090/pi")
(def graph "https://papyri.info/graph")
(def param (ref nil))
;; NOTE hard-coded file and directory locations
(def xslts {
      "DDbDP" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/current-ddbdp-rdf.xsl",
      "DCLP" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/current-dclp-rdf.xsl",
      "Historical" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/historical-rdf.xsl",
      "HGV_meta_EpiDoc" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/hgv-rdf.xsl",
      "APIS" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/apis-rdf.xsl",
      "Translations" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/translations-rdf.xsl",
      "Biblio" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/biblio-rdf.xsl"})
(def idproot "/srv/data/papyri.info/idp.data")
(def domain "papyri.info")
(def help (str "Usage: <function> [<params>]\n"
     "Functions: map-all <directory>, map-files <file list>, load-file <file>, "
     "delete-graph, delete-uri <uri>, insert-inferences <uri>."))

(defn substring-before
  [string1 string2]
  (.substring string1 0 (if (.contains string1 string2) (.indexOf string1 string2) 0)))

(defn flush-buffer [n]
  (println (str "Loading records to " server "/data"))
  (let [rdf (StringBuffer.)
        times (if (not (nil? n)) n 5000)
        connection (RDFConnectionFuseki/connect (str server "/data"))]
    (try
      (doto rdf
        (.append "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"
        xmlns:dc=\"http://purl.org/dc/elements/1.1/\"
        xmlns:dcterms=\"http://purl.org/dc/terms/\"
        xmlns:oac=\"http://www.openannotation.org/ns/\"
        xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">"))
      (dotimes [n times]
        (let [string (.poll @buffer)]
          (if (not (nil? string))
            (.append rdf string)
            (Thread/sleep 1))))
      (doto rdf
        (.append "</rdf:RDF>"))
      (let [model (ModelFactory/createDefaultModel)]
        (.read model (StringReader. (.toString rdf)) nil "RDF/XML")
        (.load connection graph model))
      (Thread/sleep 2)
      (catch Exception e
        (.println *err* (str (.getMessage e) " talking to Fuseki"))
        (.printStackTrace e)))))

(defn transform
  [file]
  (let [transformer (.load @pxslt)
        out (StringWriter.)
        dest (.newSerializer processor out)]
    (try
      (if (not (nil? @param))
        (doto transformer
          (.setParameter (QName. (first @param)) (XdmAtomicValue. (second @param)))))
      (doto transformer
        (.setSource (StreamSource. (FileInputStream. file)))
        (.setDestination dest))
      (.transform transformer)
      ;; (println (.toString out))
      (.add @buffer (.toString out))
      (catch Exception e
        (.println *err* (str (.getMessage e) " processing file " file))))))

(defn init-xslt
    [xslt]
  (when (not= xslt @xsl)
    (dosync (ref-set xsl xslt))
    (if (or (.contains xslt "ddbdp-rdf") (.contains xslt "dclp-rdf"))
      (dosync (ref-set param (list "root" idproot))))
    (let [xsl-src (StreamSource. (FileInputStream. xslt))
          processor (Processor. false)
          compiler (.newXsltCompiler processor)]
        (doto xsl-src
          (.setSystemId xslt))
        (dosync (ref-set pxslt (.compile compiler xsl-src))))))

(defn choose-xslt
  [file]
  (cond (.contains (str file) "DDbDP") (xslts "DDbDP")
    (.contains (str file) "Historical") (xslts "Historical")
    (.contains (str file) "HGV_meta_EpiDoc") (xslts "HGV_meta_EpiDoc")
    (.contains (str file) "APIS") (xslts "APIS")
    (.contains (str file) "Translations") (xslts "Translations")
    (.contains (str file) "DCLP") (xslts "DCLP")
    (.contains (str file) "Biblio") (xslts "Biblio")))

(defn format-url-query
  [filename]
  (format
    "prefix dc: <http://purl.org/dc/elements/1.1/>
    select ?uri
    from <https://papyri.info/graph>
    where {?uri dc:identifier \"%s\"}" filename))

(defn execute-query
  [query]
  (let [exec (QueryExecutionHTTP/service (str server "/query") query)]
    (.execSelect exec)))

(defn get-identifier
  [file]
  (cond
    (.contains file "DDbDP") (str "papyri.info/current/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "HGV_meta_EpiDoc") (str "papyri.info/hgv/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "APIS") (str "papyri.info/apis/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "Translations") (str "papyri.info/hgvtrans/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "DCLP") (str "papyri.info/current/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "Historical") (str "papyri.info/editions/" (substring-before file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "Biblio") (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))

(defn url-from-file
  [file]
  (if (not (.contains file "Biblio"))
    (let [answer (execute-query (format-url-query (get-identifier file)))]
      (.toString (.getResource (.next answer) "uri")))
    (str "https://papyri.info/biblio/" (get-identifier file) "/ref")))

(defn execute-update
  [request]
  (let [proc (UpdateExecutionFactory/createRemote request (str server "/update"))]
    (.execute proc)))

(defn -deleteGraph
  []
  (let [request (UpdateFactory/create)]
    (.add request "DROP ALL")
    (.add request (UpdateCreate. "https://papyri.info/graph"))
    (execute-update request)))

(defn -deleteUri
  [uri]
  (let [deletesub (str "WITH <https://papyri.info/graph>
                        DELETE { <" uri "> ?p ?r }
                        WHERE { <" uri "> ?p ?r }")
        deleteobj (str "WITH <https://papyri.info/graph>
                        DELETE { ?s ?p <" uri ">}
                        WHERE { ?s ?p <" uri ">}")
        req (UpdateFactory/create)]
    (.add req deletesub)
    (.add req deleteobj)
    (execute-update req)))

(defn -deleteRelation
  [uri]
  (let [deleterel (str "WITH <https://papyri.info/graph>
                        DELETE { ?s <" uri "> ?r }
                        WHERE { ?s <" uri "> ?r }")
        req (UpdateFactory/create)]
    (.add req deleterel)
    (execute-update req)))

(defn -deleteTriple
  [s p o]
  (let [deleterel (str "WITH <https://papyri.info/graph>
                        DELETE { <" s "> <" p "> <" o "> }
                        WHERE { <" s "> <" p "> <" o "> }")
        req (UpdateFactory/create)]
    (.add req deleterel)
    (execute-update req)))

(defn -loadFile
  [f]
  (let [connection (RDFConnectionFuseki/connect (str server "/data"))
        lang (cond 
          (.endsWith f ".rdf") RDFLanguages/RDFXML 
          (.endsWith f ".ttl") RDFLanguages/TURTLE
          :else RDFLanguages/NTRIPLES)
        model (ModelFactory/createDefaultModel)]
    (prn (str "Reading " f))
    (.. (RDFParser/create)
      (source (FileInputStream. f))
      (lang lang)
      (errorHandler (ErrorHandlerFactory/errorHandlerWarn))
      (parse model))
    (try
      (.load connection graph model)
      (catch Exception e
        (prn (str "Error loading file: " f ": " (.getMessage e)))
        (.printStackTrace e *err*)))))

(defn -insertPelagiosAnnotations
  [url]
  (if-not (nil? url)
    (let [connection (RDFConnectionFuseki/connect (str server "/data"))
          model (ModelFactory/createDefaultModel)
          pi-uri (str/replace url "/source" "/original")
          query (str "PREFIX lawd: <http://lawd.info/ontology/> "
                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                     "SELECT ?pleiades ?label "
                     "FROM <https://papyri.info/graph> "
                     "WHERE { <" pi-uri "> lawd:foundAt ?pleiades . "
                            " ?pleiades rdfs:label ?label }")
          answer (execute-query query)]
          (when (.hasNext answer)
            (let [ans (.next answer)
                  pleiades (.toString (.getResource ans "pleiades"))
                  label (.toString (.getLiteral ans "label"))
                  ann-id (DigestUtils/md5Hex (str "<" pi-uri "> <http://lawd.info/ontology/foundAt> <" pleiades ">"))
                  rdf (str "<rdf:RDF "
                              "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
                              "xmlns:dc=\"http://purl.org/dc/terms/\" "
                              "xmlns:oac=\"http://www.openannotation.org/ns/\" "
                              "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">"
                              "<rdf:Description rdf:about=\"" (str/replace url "/source" (str "/annotation/" ann-id)) "\">"
                                "<rdf:type rdf:resource=\"http://www.openannotation.org/ns/Annotation\"/>"
                                "<rdfs:label>" label "</rdfs:label>"
                                "<oac:hasBody rdf:resource=\"" pleiades "\"/>"
                                "<oac:hasTarget rdf:resource=\"" url "\"/>"
                                "<oac:motivatedBy rdf:resource=\"http://www.openannotation.org/ns/linking\"/>"
                              "</rdf:Description>"
                            "</rdf:RDF>")]
              (.read model (StringReader. rdf) nil "RDF/XML")
              (.load connection graph model))))
    (let [query (str "PREFIX lawd: <http://lawd.info/ontology/> "
                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                     "SELECT ?uri ?pleiades ?label "
                     "FROM <https://papyri.info/graph> "
                     "WHERE { ?uri lawd:foundAt ?pleiades . "
                            " ?pleiades rdfs:label ?label }")
          answer (execute-query query)
          nthreads (.availableProcessors (Runtime/getRuntime))
          pool (Executors/newFixedThreadPool nthreads)]
      (dosync (ref-set buffer (ConcurrentLinkedQueue.) ))
      (while (.hasNext answer)
        (let [ans (.next answer)
              uri (.toString (.getResource ans "uri"))
              pleiades (.toString (.getResource ans "pleiades"))
              label (.toString (.getLiteral ans "label"))
              ann-id (DigestUtils/md5Hex (str "<" uri "> <http://lawd.info/ontology/foundAt> <" pleiades ">"))]
          (.add @buffer (str "<rdf:Description rdf:about=\"" (str/replace uri "/original" (str "/annotation/" ann-id)) "\">"
                              "<rdf:type rdf:resource=\"http://www.openannotation.org/ns/Annotation\"/>"
                              "<rdfs:label>" label "</rdfs:label>"
                              "<oac:hasBody rdf:resource=\"" pleiades "\"/>"
                              "<oac:hasTarget rdf:resource=\"" (str/replace uri "/original" "") "\"/>"
                              "<oac:motivatedBy rdf:resource=\"http://www.openannotation.org/ns/linking\"/>"
                            "</rdf:Description>"))
          (when (> (count @buffer) 5000)
            (flush-buffer nil))))
        (flush-buffer (count @buffer)))))

(defn -insertInferences
  [url]
  (if (not (nil? url))
    (let [request (UpdateFactory/create)
          haspart (str "PREFIX dc: <http://purl.org/dc/terms/> "
                       "WITH <https://papyri.info/graph> "
                       "INSERT {?s dc:hasPart <" url ">} "
                       "WHERE { <" url "> dc:isPartOf ?s}")
          relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                        "WITH <https://papyri.info/graph> "
                        "INSERT {?s dc:relation <" url ">} "
                        "WHERE { <" url "> dc:relation ?s "
                        "FILTER regex(\"" url "\", \"^https://papyri.info\") "
                        "FILTER regex(str(?s), \"^https://papyri.info\")}")
          converse-relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                 "WITH <https://papyri.info/graph> "
                                 "INSERT {<" url "> dc:relation ?o} "
                                 "WHERE { ?o dc:relation <" url "> "
                                 "FILTER regex(\"" url "\", \"^https://papyri.info\") "
                                 "FILTER regex(str(?o), \"^https://papyri.info\")}")
          transitive-rels (str "PREFIX dc: <http://purl.org/dc/terms/> "
                               "WITH <https://papyri.info/graph> "
                               "INSERT {<" url "> dc:relation ?o2} "
                               "WHERE { <" url "> dc:relation ?o1 . "
                               "?o1 dc:relation ?o2 "
                               "FILTER (!sameTerm(<" url ">, ?o2)) "
                               "FILTER regex(\"" url "\", \"^https://papyri.info\") "
                               "FILTER regex(str(?o1), \"^https://papyri.info\") "
                               "FILTER regex(str(?o2), \"^https://papyri.info\")}")
          converse-rels (str "PREFIX dc: <http://purl.org/dc/terms/> "
                               "WITH <https://papyri.info/graph> "
                               "INSERT {?o2 dc:relation <" url ">} "
                               "WHERE { ?o1 dc:relation <" url "> . "
                               "?o1 dc:relation ?o2 "
                               "FILTER (!sameTerm(<" url ">, ?o2)) "
                               "FILTER regex(\"" url "\", \"^https://papyri.info\") "
                               "FILTER regex(str(?o1), \"^https://papyri.info\") "
                               "FILTER regex(str(?o2), \"^https://papyri.info\")}")
          version (str "PREFIX dc: <http://purl.org/dc/terms/> "
                        "WITH <https://papyri.info/graph> "
                        "INSERT {?s dc:hasVersion <" url ">} "
                        "WHERE { <" url "> dc:isVersionOf ?s}")
          converse-version (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                 "WITH <https://papyri.info/graph> "
                                 "INSERT {<" url "> dc:isVersionOf ?o} "
                                 "WHERE { ?o dc:hasVersion <" url ">}")]
      (.add request haspart)
      (.add request relation)
      (.add request transitive-rels)
      (.add request converse-rels)
      (.add request converse-relation)
      (.add request version)
      (.add request converse-version)
      (execute-update request))
    (let [request (UpdateFactory/create)
          hasPart (str "PREFIX dc: <http://purl.org/dc/terms/> "
                       "WITH <https://papyri.info/graph> "
                       "INSERT{?s dc:hasPart ?o} "
                       "WHERE { ?o dc:isPartOf ?s}")
          relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                        "WITH <https://papyri.info/graph> "
                        "INSERT {?s dc:relation ?o} "
                        "WHERE { ?o dc:relation ?s "
                        "FILTER regex(str(?s), \"^https://papyri.info\") "
                        "FILTER regex(str(?o), \"^https://papyri.info\")}")
          images (str "PREFIX dc: <http://purl.org/dc/terms/> "
                      "WITH <https://papyri.info/graph> "
                      "INSERT { ?r1 dc:relation ?r2 } "
                      "WHERE { "
                      "?c dc:isPartOf <https://papyri.info/apis> . "
                      "?i dc:isPartOf ?c . "
                      "?i dc:relation ?r1 . "
                      "?i dc:relation ?r2 . "
                      "FILTER ( regex(str(?r1), \"^https://papyri.info/ddbdp\") || regex(str(?r1), \"^https://papyri.info/hgv\") || regex(str(?r1), \"^http://www.trismegistos.org\")) "
                      "FILTER  regex(str(?r2), \"^https://papyri.info/apis/[^/]+/images\")}")
          transitive-rels (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                "WITH <https://papyri.info/graph> "
                                "INSERT {?s dc:relation ?o2} "
                                "WHERE { ?s dc:relation ?o1 . "
                                        "?o1 dc:relation ?o2 "
                                "FILTER (!sameTerm(?s, ?o2)) "
                                "FILTER regex(str(?s), \"^https://papyri.info\") "
                                "FILTER regex(str(?o1), \"^https://papyri.info\") "
                                "FILTER regex(str(?o2), \"^https://papyri.info\")}")]
      (println relation)
      (println transitive-rels)
      (.add request hasPart)
      (.add request relation)
      (.add request images)
      (.add request transitive-rels)
      (.add request relation) ;; repeat in order to pick up new dc:relations
      (execute-update request))))

(defn load-map
  [file]
  (def nthreads (.availableProcessors (Runtime/getRuntime)))
  (dosync (ref-set buffer (ConcurrentLinkedQueue.) ))
  (let [xsl (choose-xslt file)]
    (init-xslt xsl))
  (let [request (UpdateFactory/create)]
    (.add request "CREATE SILENT GRAPH <https://papyri.info/graph>")
    (execute-update request))
  (let [pool (Executors/newFixedThreadPool nthreads)
      files (file-seq (File. file))
      tasks (map (fn [x]
        (fn []
          (transform x)
          (when (> (count @buffer) 5000)
            (flush-buffer nil))))
        (filter #(.endsWith (.getName %) ".xml") files))]
      (doseq [future (.invokeAll pool tasks)]
        (.get future))
      (doto pool
        (.shutdown)))
  (flush-buffer (count @buffer)))

(defn -mapFiles
  [files]
  (println (str "Mapping " (.size files) " files."))
  (dosync (ref-set buffer (ConcurrentLinkedQueue.) ))
  (when (> (.size files) 0)
    (doseq [file files]
       (let [xsl (choose-xslt file)]
         (init-xslt xsl))
       (transform file))
    (flush-buffer (count @buffer))))


(defn -mapAll
  [args]
  (if (> (count args) 0)
    (do
      (load-map (first args))
      (-insertInferences (first args))
      (-insertPelagiosAnnotations (first args)))
    (do
      (println "Deleting Relations")
      (-deleteRelation "http://purl.org/dc/terms/relation")
      (-deleteRelation "http://purl.org/dc/terms/replaces")
      (-deleteRelation "http://purl.org/dc/terms/isReplacedBy")
      (-deleteRelation "http://purl.org/dc/terms/isPartOf")
      (-deleteRelation "http://purl.org/dc/terms/isVersionOf")
      (-deleteRelation "http://purl.org/dc/terms/hasVersion")
      (-deleteRelation "http://purl.org/dc/terms/source")
      (-deleteRelation "http://purl.org/dc/terms/identifier")
      (-deleteRelation "http://purl.org/dc/terms/bibliographicCitation")
      (-deleteRelation "http://www.w3.org/2000/01/rdf-schema#label")
      (-deleteRelation "http://purl.org/ontology/bibo/translationOf")
      (-deleteRelation "http://xmlns.com/foaf/0.1/page")
      (println "Processing DDbDP")
      (load-map (str idproot "/DDbDP"))
      (println "Processing HGV_meta_EpiDoc")
      (load-map (str idproot "/HGV_meta_EpiDoc"))
      (println "Processing APIS")
      (load-map (str idproot "/APIS"))
      (println "Processing Translations")
      (load-map (str idproot "/Translations"))
      (println "Processing DCLP")
      (load-map (str idproot "/DCLP"))
      (println "Processing Historical")
      (load-map (str idproot "/Historical"))
      (println "Processing Bibliography")
      (load-map (str idproot "/Biblio"))
      (-loadFile "/srv/data/papyri.info/idp.data/RDF/collection.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/0.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/1.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/2.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/3.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/4.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/5.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/6.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/7.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/8.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/9.rdf")
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/gothenburg.rdf")
      (doseq [f (filter #(.startsWith (.getName %) "places-") (file-seq (File. "/srv/data/papyri.info/pleiades")))]
        (-loadFile (.getAbsolutePath f)))
      (-insertInferences nil)
      (-insertPelagiosAnnotations nil))))

(defn -main
  [& args]
  (if (> (count args) 0)
    (let [function (first args)]
      (cond (= function "map-all") (-mapAll (rest args))
            (= function "map-files") (-mapFiles (rest args))
            (= function "load-file") (-loadFile (second args))
            (= function "delete-graph") (-deleteGraph)
            (= function "delete-uri") (-deleteUri (second args))
            (= function "delete-relation") (-deleteRelation (second args))
            (= function "insert-inferences") (if (> (count args) 1)
              (for [file (rest args)]
                (-insertInferences (url-from-file file)))
              (-insertInferences nil))
            (= function "insert-pelagios") (if (> (count args) 1)
              (for [file (rest args)]
                (-insertPelagiosAnnotations (url-from-file file)))
              (-insertPelagiosAnnotations nil))
            (= function "help") (print help)))
    ((print help))))
