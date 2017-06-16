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
           (javax.activation MimeType)
           (javax.xml.transform Templates Transformer)
           (javax.xml.transform.stream StreamSource StreamResult)
           (net.sf.saxon Configuration PreparedStylesheet TransformerFactoryImpl)
           (net.sf.saxon.lib FeatureKeys StandardErrorListener StandardURIResolver)
           (net.sf.saxon.trans CompilerInfo XPathException)
           (org.apache.jena.fuseki.http DatasetAdapter DatasetGraphAccessorHTTP UpdateRemote)
           (com.hp.hpl.jena.graph Node)
           (com.hp.hpl.jena.query QueryExecutionFactory)
           (com.hp.hpl.jena.rdf.model Model ModelFactory)
           (com.hp.hpl.jena.sparql.modify.request UpdateCreate UpdateLoad)
           (com.hp.hpl.jena.update Update UpdateFactory UpdateRequest)
           (com.hp.hpl.jena.sparql.lang UpdateParser)
           (org.apache.commons.codec.digest DigestUtils)))

(def xsl (ref nil))
(def pxslt (ref nil))
(def buffer (ref nil))
(def flushing (ref false))
(def output (ref nil))
(def server "http://localhost:8090/pi")
(def graph "http://papyri.info/graph")
(def param (ref nil))
;; NOTE hard-coded file and directory locations
(def xslts {"DDB_EpiDoc_XML" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/ddbdp-rdf.xsl",
      "HGV_meta_EpiDoc" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/hgv-rdf.xsl",
      "APIS" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/apis-rdf.xsl",
      "HGV_trans_EpiDoc" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/hgvtrans-rdf.xsl",
      "Biblio" "/srv/data/papyri.info/git/navigator/pn-mapping/xslt/biblio-rdf.xsl"})
(def idproot "/srv/data/papyri.info/idp.data")
(def ddbroot "/srv/data/papyri.info/idp.data/DDB_EpiDoc_XML")
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
        dga (DatasetGraphAccessorHTTP. (str server "/data"))
        adapter (DatasetAdapter. dga)]
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
        (.add adapter graph model))
      (Thread/sleep 2)
      (catch Exception e
        (.println *err* (str (.getMessage e) " talking to Fuseki"))
        (.printStackTrace e)))))

(defn transform
  [file]
  (let [transformer (.newTransformer @pxslt)
        out (StringWriter.)
        outstr (StreamResult. out)]
    (try
      (if (not (nil? @param))
        (doto transformer
          (.setParameter (first @param) (second @param))))
      (.transform transformer (StreamSource. (FileInputStream. file)) outstr)
      ;; (println (.toString out))
      (.add @buffer (.toString out))
      (catch Exception e
        (.println *err* (str (.getMessage e) " processing file " file))))))

(defn init-xslt
    [xslt]
  (when (not= xslt @xsl)
    (dosync (ref-set xsl xslt))
    (if (.contains xslt "ddbdp-rdf")
      (dosync (ref-set param (list "root" idproot)))
      (dosync (ref-set param (list "DDB-root" ddbroot))))
    (let [xsl-src (StreamSource. (FileInputStream. xslt))
        configuration (Configuration.)
        compiler-info (CompilerInfo.)]
        (doto xsl-src
          (.setSystemId xslt))
        (doto compiler-info
          (.setErrorListener (StandardErrorListener.))
          (.setURIResolver (StandardURIResolver. configuration)))
        (dosync (ref-set pxslt (PreparedStylesheet/compile xsl-src configuration compiler-info))))))

(defn choose-xslt
  [file]
  (cond (.contains (str file) "DDB_EpiDoc_XML") (xslts "DDB_EpiDoc_XML")
    (.contains (str file) "HGV_meta_EpiDoc") (xslts "HGV_meta_EpiDoc")
    (.contains (str file) "APIS") (xslts "APIS")
    (.contains (str file) "HGV_trans_EpiDoc") (xslts "HGV_trans_EpiDoc")
    (.contains (str file) "Biblio") (xslts "Biblio")))

(defn format-url-query
  [filename]
  (format
    "prefix dc: <http://purl.org/dc/elements/1.1/>
    select ?uri
    from <http://papyri.info/graph>
    where {?uri dc:identifier \"%s\"}" filename))

(defn execute-query
  [query]
  (let [exec (QueryExecutionFactory/sparqlService (str server "/query") query)]
    (.execSelect exec)))

(defn get-identifier
  [file]
  (cond
    (.contains file "DDB_EpiDoc_XML") (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml")
    (.contains file "HGV_meta_EpiDoc") (str "papyri.info/hgv/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "APIS") (str "papyri.info/apis/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "HGV_trans_EpiDoc") (str "papyri.info/hgvtrans/" (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml"))
    (.contains file "Biblio") (substring-before (.substring file (inc (.lastIndexOf file "/"))) ".xml")))

(defn url-from-file
  [file]
  (if (not (.contains file "Biblio"))
    (let [answer (execute-query (format-url-query (get-identifier file)))]
      (.toString (.getResource (.next answer) "uri")))
    (str "http://papyri.info/biblio/" (get-identifier file) "/ref")))

(defn -deleteGraph
  []
  (let [request (UpdateFactory/create)]
    (.add request "DROP ALL")
    (.add request (UpdateCreate. "http://papyri.info/graph"))
    (UpdateRemote/execute request (str server "/update") )))

(defn -deleteUri
  [uri]
  (let [deletesub (str "WITH <http://papyri.info/graph>
                        DELETE { <" uri "> ?p ?r }
                        WHERE { <" uri "> ?p ?r }")
        deleteobj (str "WITH <http://papyri.info/graph>
                        DELETE { ?s ?p <" uri ">}
                        WHERE { ?s ?p <" uri ">}")
        req (UpdateFactory/create)]
    (.add req deletesub)
    (.add req deleteobj)
    (UpdateRemote/execute req (str server "/update") )))

(defn -deleteRelation
  [uri]
  (let [deleterel (str "WITH <http://papyri.info/graph>
                        DELETE { ?s <" uri "> ?r }
                        WHERE { ?s <" uri "> ?r }")
        req (UpdateFactory/create)]
    (.add req deleterel)
    (UpdateRemote/execute req (str server "/update") )))

(defn -deleteTriple
  [s p o]
  (let [deleterel (str "WITH <http://papyri.info/graph>
                        DELETE { <" s "> <" p "> <" o "> }
                        WHERE { <" s "> <" p "> <" o "> }")
        req (UpdateFactory/create)]
    (.add req deleterel)
    (UpdateRemote/execute req (str server "/update") )))

(defn -loadFile
  [f]
  (let [dga (DatasetGraphAccessorHTTP. (str server "/data"))
        adapter (DatasetAdapter. dga)
        model (ModelFactory/createDefaultModel)]
    (.read model (FileInputStream. f) nil (if (.endsWith f ".rdf") "RDF/XML" "N3"))
    (try
      (.add adapter graph model)
      (catch Exception e
        (println (str "Error loading file: " f " ...trying again."))
        (.printStackTrace *err*)
        (Thread/sleep 1000)
        (try 
          (.add adapter graph model)
          (catch Exception ex
            (println "Failed to load file.")))))))
    

(defn -insertPelagiosAnnotations
  [url]
  (if-not (nil? url)
    (let [dga (DatasetGraphAccessorHTTP. (str server "/srv/data"))
          adapter (DatasetAdapter. dga)
          model (ModelFactory/createDefaultModel)
          pi-uri (str/replace url "/source" "/original")
          query (str "PREFIX lawd: <http://lawd.info/ontology/> "
                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                     "SELECT ?pleiades ?label "
                     "FROM <http://papyri.info/graph> "
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
              (.add adapter graph model))))
    (let [query (str "PREFIX lawd: <http://lawd.info/ontology/> "
                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                     "SELECT ?uri ?pleiades ?label "
                     "FROM <http://papyri.info/graph> "
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
                       "WITH <http://papyri.info/graph> "
                       "INSERT {?s dc:hasPart <" url ">} "
                       "WHERE { <" url "> dc:isPartOf ?s}")
          relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                        "WITH <http://papyri.info/graph> "
                        "INSERT {?s dc:relation <" url ">} "
                        "WHERE { <" url "> dc:relation ?s "
                        "FILTER regex(\"" url "\", \"^http://papyri.info\") "
                        "FILTER regex(str(?s), \"^http://papyri.info\")}")
          converse-relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                 "WITH <http://papyri.info/graph> "
                                 "INSERT {<" url "> dc:relation ?o} "
                                 "WHERE { ?o dc:relation <" url "> "
                                 "FILTER regex(\"" url "\", \"^http://papyri.info\") "
                                 "FILTER regex(str(?o), \"^http://papyri.info\")}")
          transitive-rels (str "PREFIX dc: <http://purl.org/terms/> "
                               "WITH <http://papyri.info/graph> "
                               "INSERT {<" url "> dc:relation ?o2} "
                               "WHERE { <" url "> dc:relation ?o1 . "
                               "?o1 dc:relation ?o2 "
                               "FILTER (!sameTerm(<" url ">, ?o2))}")
          converse-rels (str "PREFIX dc: <http://purl.org/terms/> "
                               "WITH <http://papyri.info/graph> "
                               "INSERT {?o2 dc:relation <" url ">} "
                               "WHERE { ?o1 dc:relation <" url "> . "
                               "?o1 dc:relation ?o2 "
                               "FILTER (!sameTerm(<" url ">, ?o2))}")
          replaces-rels (str "PREFIX dc: <http://purl.org/terms/> "
                             "WITH <http://papyri.info/graph> "
                             "INSERT {<" url "> dc:replaces ?o} "
                             "WHERE {<" url "> dc:replaces ?s .
                                     ?s dc:replaces ?o }")
          replaces-relations (str "PREFIX dc: <http://purl.org/terms/> "
                             "WITH <http://papyri.info/graph> "
                             "INSERT {<" url "> dc:relation ?o} "
                             "WHERE {<" url "> dc:replaces ?s .
                                     ?o dc:relation ?s }")]
      (.add request haspart)
      (.add request relation)
      (.add request transitive-rels)
      (.add request converse-rels)
      (.add request converse-relation)
      (.add request replaces-rels)
      (.add request replaces-relations)
      (UpdateRemote/execute request (str server "/update") ))
    (let [request (UpdateFactory/create)
          hasPart (str "PREFIX dc: <http://purl.org/dc/terms/> "
                       "WITH <http://papyri.info/graph> "
                       "INSERT{?s dc:hasPart ?o} "
                       "WHERE { ?o dc:isPartOf ?s}")
          relation (str "PREFIX dc: <http://purl.org/dc/terms/> "
                        "WITH <http://papyri.info/graph> "
                        "INSERT {?s dc:relation ?o} "
                        "WHERE { ?o dc:relation ?s}")
          translations (str "PREFIX dc: <http://purl.org/dc/terms/> "
                            "WITH <http://papyri.info/graph> "
                            "INSERT { ?r1 dc:relation ?r2 } "
                            "WHERE { "
                            "?i dc:relation ?r1 . "
                            "?i dc:relation ?r2 . "
                            "FILTER  regex(str(?i), \"^http://papyri.info/hgv\") "
                            "FILTER  regex(str(?r1), \"^http://papyri.info/ddbdp\") "
                            "FILTER  regex(str(?r2), \"^http://papyri.info/hgvtrans\")}")
          images (str "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                      "PREFIX dcterms: <http://purl.org/dc/terms/>"
                      "WITH <http://papyri.info/graph> "
                      "INSERT { ?r1 dcterms:relation ?r2 } "
                      "WHERE { "
                      "?c dcterms:isPartOf <http://papyri.info/apis> . "
                      "?i dcterms:isPartOf ?c . "
                      "?i dcterms:relation ?r1 . "
                      "?i dcterms:relation ?r2 . "
                      "FILTER ( regex(str(?r1), \"^http://papyri.info/ddbdp\") || regex(str(?r1), \"^http://papyri.info/hgv\") || regex(str(?r1), \"^http://www.trismegistos.org\")) "
                      "FILTER  regex(str(?r2), \"^http://papyri.info/apis/[^/]+/images\")}")
          transitive-rels (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                "WITH <http://papyri.info/graph> "
                                "INSERT {?s dc:relation ?o2} "
                                "WHERE { ?s dc:relation ?o1 . "
                                         "?o1 dc:relation ?o2 "
                                "FILTER (!sameTerm(?s, ?o2))}")
          replaces-rels (str "PREFIX dc: <http://purl.org/dc/terms/> "
                             "WITH <http://papyri.info/graph> "
                             "INSERT {?s dc:replaces ?o} "
                             "WHERE { ?s dc:replaces ?s2 .
                                      ?s2 dc:replaces ?o }")
          replaces-relations (str "PREFIX dc: <http://purl.org/dc/terms/> "
                                  "WITH <http://papyri.info/graph> "
                                  "INSERT {?s dc:relation ?o} "
                                  "WHERE { ?s dc:replaces ?s2 .
                                           ?o dc:relation ?s2 "
                                  "FILTER (!sameTerm(?s, ?o))}")]
      (.add request hasPart)
      (.add request relation)
      (.add request translations)
      (.add request images)
      (.add request transitive-rels)
      (.add request replaces-rels)
      (.add request replaces-relations)
      (.add request relation) ;; repeat in order to pick up new dc:relations
      (UpdateRemote/execute request (str server "/update") ))))

(defn load-map
  [file]
  (def nthreads (.availableProcessors (Runtime/getRuntime)))
  (dosync (ref-set buffer (ConcurrentLinkedQueue.) ))
  (let [xsl (choose-xslt file)]
    (init-xslt xsl))
  (let [request (UpdateFactory/create)]
    (.add request "CREATE SILENT GRAPH <http://papyri.info/graph>")
    (UpdateRemote/execute request (str server "/update") ))
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
      (-deleteRelation "http://purl.org/dc/terms/identifier")
      (-deleteRelation "http://purl.org/dc/terms/bibliographicCitation")
      (-deleteRelation "http://www.w3.org/2000/01/rdf-schema#label")
      (println "Processing DDB_EpiDoc_XML")
      (load-map (str idproot "/DDB_EpiDoc_XML"))
      (println "Processing HGV_meta_EpiDoc")
      (load-map (str idproot "/HGV_meta_EpiDoc"))
      (println "Processing APIS")
      (load-map (str idproot "/APIS"))
      (println "Processing HGV_trans_EpiDoc")
      (load-map (str idproot "/HGV_trans_EpiDoc"))
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
      (-loadFile "/srv/data/papyri.info/git/navigator/pn-mapping/sources/glrt.n3")
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
